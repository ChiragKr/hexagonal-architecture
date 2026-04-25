package com.pluralkraft.notification.adapters.outbound.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.pluralkraft.notification.domain.model.DeliveryStatus;

/**
 * MongoDB entity representing a delivery record.
 * This entity maps to the "deliveries" collection in MongoDB
 * and is used to persist delivery information.
 */
@Document(collection = "deliveries")
public class MongoDeliveryEntity {

    /**
     * The unique identifier for the delivery record.
     * This field is managed by MongoDB and is annotated with @Id.
     */
    @Id
    private String id;

    /**
     * The identifier of the notification associated with this delivery.
     */
    private String notificationId;

    /**
     * The current status of the delivery.
     */
    private DeliveryStatus status;

    /**
     * Default constructor for MongoDB entity mapping.
     * This constructor is required for Spring Data MongoDB to instantiate objects.
     */
    public MongoDeliveryEntity() {
    }

    /**
     * Constructs a new MongoDeliveryEntity with the specified properties.
     *
     * @param id the unique identifier for the delivery
     * @param notificationId the identifier of the associated notification
     * @param status the current status of the delivery
     */
    public MongoDeliveryEntity(String id, String notificationId, DeliveryStatus status) {
        this.id = id;
        this.notificationId = notificationId;
        this.status = status;
    }

    /**
     * Gets the unique identifier for the delivery.
     *
     * @return the delivery identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the delivery.
     *
     * @param id the delivery identifier to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the identifier of the notification associated with this delivery.
     *
     * @return the notification identifier
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Sets the identifier of the notification associated with this delivery.
     *
     * @param notificationId the notification identifier to set
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Gets the current status of the delivery.
     *
     * @return the delivery status
     */
    public DeliveryStatus getStatus() {
        return status;
    }

    /**
     * Sets the current status of the delivery.
     *
     * @param status the delivery status to set
     */
    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
}