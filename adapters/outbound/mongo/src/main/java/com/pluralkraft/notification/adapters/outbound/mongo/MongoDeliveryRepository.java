package com.pluralkraft.notification.adapters.outbound.mongo;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * MongoDB implementation of the DeliveryRepository port.
 * This repository handles persistence operations for Delivery entities
 * using MongoDB as the underlying data store.
 */
public class MongoDeliveryRepository implements DeliveryRepository {

    private MongoTemplate mongoTemplate;

    /**
     * Constructs a new MongoDeliveryRepository with the specified MongoTemplate.
     *
     * @param mongoTemplate the Spring Data MongoDB template to use for database operations
     */
    public MongoDeliveryRepository(
        MongoTemplate mongoTemplate
    ) {
        this.mongoTemplate = mongoTemplate;
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
        MongoDeliveryEntity entity = new MongoDeliveryEntity(
            delivery.id(),
            delivery.notificationId(),
            delivery.status()
        );

        entity = mongoTemplate.save(entity);

        return new Delivery(
            entity.getId(),
            entity.getNotificationId(),
            entity.getStatus()
        );
    }

    /**
     * Updates the status of a Delivery entity in MongoDB.
     *
     * @param deliveryId the ID of the delivery to update
     * @param status the new status to set
     */
    @Override
    public void updateStatus(String deliveryId, DeliveryStatus status) {
        Query query = new Query(Criteria.where("id").is(deliveryId));
        Update update = new Update().set("status", status);

        mongoTemplate.updateFirst(query, update, MongoDeliveryEntity.class);
    }
}
