package com.pluralkraft.notification.adapters.outbound.mongo;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(MongoDeliveryRepository.class);
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
        if (delivery == null) {
            throw new IllegalArgumentException("Delivery cannot be null");
        }
        logger.debug("Saving delivery: {}", delivery.id());

        MongoDeliveryEntity entity = new MongoDeliveryEntity(
            delivery.id(),
            delivery.notificationId(),
            delivery.status()
        );

        try {
            entity = mongoTemplate.save(entity);
        } catch (Exception e) {
            logger.error("Failed to save delivery to MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save delivery to MongoDB: " + e.getMessage(), e);
        }

        logger.info("Successfully saved delivery with ID: {}", entity.getId());

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
        logger.debug("Updating status for delivery ID: {}", deliveryId);

        if (deliveryId == null || deliveryId.isEmpty()) {
            throw new IllegalArgumentException("Delivery ID cannot be null or empty");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        Query query = new Query(Criteria.where("id").is(deliveryId));
        Update update = new Update().set("status", status);

        try {
            mongoTemplate.updateFirst(query, update, MongoDeliveryEntity.class);
            logger.info("Successfully updated status for delivery ID: {}", deliveryId);
        } catch (Exception e) {
            logger.error("Failed to update status for delivery ID {}: {}", deliveryId, e.getMessage(), e);
            throw new RuntimeException("Failed to update status for delivery: " + e.getMessage(), e);
        }
    }
}
