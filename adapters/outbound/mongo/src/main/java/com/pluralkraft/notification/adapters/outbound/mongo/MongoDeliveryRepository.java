package com.pluralkraft.notification.adapters.outbound.mongo;

import static com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryEntity.FIELD_ID;
import static com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryEntity.FIELD_LOCKED_BY;
import static com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryEntity.FIELD_LOCKED_UNTIL;
import static com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryEntity.FIELD_RETRY_COUNT;
import static com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryEntity.FIELD_STATUS;
import static com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryEntity.FIELD_UPDATED_AT;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.ports.out.DeliveryRepository;

/**
 * MongoDB implementation of the DeliveryRepository port.
 * This repository handles persistence operations for Delivery entities
 * using MongoDB as the underlying data store.
 */
public class MongoDeliveryRepository implements DeliveryRepository {

    private final MongoTemplate mongoTemplate;
    private final Clock clock;

    /**
     * Constructs a new MongoDeliveryRepository with the specified MongoTemplate.
     *
     * @param mongoTemplate the Spring Data MongoDB template to use for database operations
     */
    public MongoDeliveryRepository(
        MongoTemplate mongoTemplate
    ) {
        this(mongoTemplate, Clock.systemUTC());
    }

    /**
     * Constructs a new MongoDeliveryRepository with the specified MongoTemplate and clock.
     * Ensures the indexes declared on {@link MongoDeliveryEntity} exist.
     *
     * @param mongoTemplate the Spring Data MongoDB template to use for database operations
     * @param clock the clock used for timestamps and claim leases
     */
    public MongoDeliveryRepository(
        MongoTemplate mongoTemplate,
        Clock clock
    ) {
        this.mongoTemplate = mongoTemplate;
        this.clock = clock;
        ensureIndexes();
    }

    /**
     * Saves a Delivery entity to MongoDB.
     * Converts the Delivery domain model to MongoDeliveryEntity and persists it.
     *
     * @param delivery the Delivery entity to save
     * @return the saved Delivery entity with updated information from the database
     */
    @Override
    public Delivery save(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Delivery cannot be null");
        }

        Instant now = clock.instant();
        MongoDeliveryEntity entity = new MongoDeliveryEntity(
            delivery.id(),
            delivery.notificationId(),
            delivery.payload(),
            delivery.status(),
            delivery.retryCount()
        );
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return toDomain(mongoTemplate.save(entity));
    }

    /**
     * Updates the status of a Delivery entity in MongoDB.
     *
     * @param deliveryId the ID of the delivery to update
     * @param status the new status to set
     */
    @Override
    public void updateStatus(String deliveryId, DeliveryStatus status) {
        Query query = new Query(Criteria.where(FIELD_ID).is(deliveryId));
        Update update = new Update()
            .set(FIELD_STATUS, status)
            .set(FIELD_UPDATED_AT, clock.instant());

        mongoTemplate.updateFirst(query, update, MongoDeliveryEntity.class);
    }

    /**
     * Claims failed deliveries in three steps:
     * <ol>
     *   <li>select candidate ids (most recently updated first),</li>
     *   <li>atomically mark them with {@code claimToken} only if they are still failed and unclaimed,</li>
     *   <li>read back the deliveries that carry {@code claimToken}.</li>
     * </ol>
     * Each document update is atomic, so concurrent instances racing on the same candidates
     * end up with disjoint sets.
     */
    @Override
    public List<Delivery> claimFailedDeliveries(int limit, String claimToken, Duration lease) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (claimToken == null || claimToken.isBlank()) {
            throw new IllegalArgumentException("claimToken cannot be blank");
        }
        if (lease == null || lease.isZero() || lease.isNegative()) {
            throw new IllegalArgumentException("lease must be positive");
        }

        Instant now = clock.instant();

        Query candidates = new Query(claimable(now))
            .with(Sort.by(Sort.Direction.DESC, FIELD_UPDATED_AT))
            .limit(limit);
        candidates.fields().include(FIELD_ID);
        List<String> ids = mongoTemplate.find(candidates, MongoDeliveryEntity.class)
            .stream()
            .map(MongoDeliveryEntity::getId)
            .toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        Query claim = new Query(new Criteria().andOperator(
            Criteria.where(FIELD_ID).in(ids),
            claimable(now)
        ));
        Update lock = new Update()
            .set(FIELD_LOCKED_BY, claimToken)
            .set(FIELD_LOCKED_UNTIL, now.plus(lease));
        mongoTemplate.updateMulti(claim, lock, MongoDeliveryEntity.class);

        Query owned = new Query(Criteria.where(FIELD_LOCKED_BY).is(claimToken))
            .with(Sort.by(Sort.Direction.DESC, FIELD_UPDATED_AT));
        return mongoTemplate.find(owned, MongoDeliveryEntity.class)
            .stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public boolean completeRetryAttempt(
        String deliveryId,
        String claimToken,
        DeliveryStatus status,
        int retryCount
    ) {
        if (deliveryId == null || deliveryId.isBlank()) {
            throw new IllegalArgumentException("deliveryId cannot be blank");
        }
        if (claimToken == null || claimToken.isBlank()) {
            throw new IllegalArgumentException("claimToken cannot be blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }

        Query query = new Query(Criteria.where(FIELD_ID).is(deliveryId)
            .and(FIELD_LOCKED_BY).is(claimToken));
        Update update = new Update()
            .set(FIELD_STATUS, status)
            .set(FIELD_RETRY_COUNT, retryCount)
            .set(FIELD_UPDATED_AT, clock.instant())
            .unset(FIELD_LOCKED_BY)
            .unset(FIELD_LOCKED_UNTIL);

        return mongoTemplate.updateFirst(query, update, MongoDeliveryEntity.class).getModifiedCount() > 0;
    }

    /**
     * Criteria matching failed deliveries that are not claimed or whose claim has expired.
     */
    private Criteria claimable(Instant now) {
        return new Criteria().andOperator(
            Criteria.where(FIELD_STATUS).is(DeliveryStatus.FAILURE),
            new Criteria().orOperator(
                Criteria.where(FIELD_LOCKED_UNTIL).is(null),
                Criteria.where(FIELD_LOCKED_UNTIL).lt(now)
            )
        );
    }

    private void ensureIndexes() {
        var indexOps = mongoTemplate.indexOps(MongoDeliveryEntity.class);
        new MongoPersistentEntityIndexResolver(mongoTemplate.getConverter().getMappingContext())
            .resolveIndexFor(MongoDeliveryEntity.class)
            .forEach(indexOps::createIndex);
    }

    private Delivery toDomain(MongoDeliveryEntity entity) {
        return new Delivery(
            entity.getId(),
            entity.getNotificationId(),
            entity.getPayload(),
            entity.getStatus(),
            entity.getRetryCount()
        );
    }
}
