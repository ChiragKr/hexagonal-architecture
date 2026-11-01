package com.pluralkraft.notification.adapters.outbound.mongo;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.pluralkraft.notification.domain.model.DeliveryStatus;

/**
 * MongoDB entity representing a delivery record.
 * This entity maps to the "deliveries" collection in MongoDB
 * and is used to persist delivery information.
 *
 * <p>The {@code lockedBy}/{@code lockedUntil} fields implement a lease-based claim so that,
 * with multiple application instances, a failed delivery is retried by a single instance only.
 */
@Document(collection = "deliveries")
@CompoundIndex(name = "status_updatedAt", def = "{'status': 1, 'updatedAt': -1}")
public class MongoDeliveryEntity {

    /** Field names used in queries. */
    static final String FIELD_ID = "id";
    static final String FIELD_STATUS = "status";
    static final String FIELD_RETRY_COUNT = "retryCount";
    static final String FIELD_LOCKED_BY = "lockedBy";
    static final String FIELD_LOCKED_UNTIL = "lockedUntil";
    static final String FIELD_UPDATED_AT = "updatedAt";

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
     * The payload of the notification, persisted so the notification can be retried.
     */
    private String payload;

    /**
     * The current status of the delivery.
     */
    private DeliveryStatus status;

    /**
     * The number of retries already performed for this delivery.
     */
    private int retryCount;

    /**
     * Claim token of the instance currently retrying this delivery, or {@code null} when unclaimed.
     */
    @Indexed
    private String lockedBy;

    /**
     * Time at which the current claim expires, or {@code null} when unclaimed.
     */
    private Instant lockedUntil;

    /**
     * Time at which the delivery was created.
     */
    private Instant createdAt;

    /**
     * Time at which the delivery was last updated.
     */
    private Instant updatedAt;

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
     * @param payload the notification payload
     * @param status the current status of the delivery
     * @param retryCount the number of retries already performed
     */
    public MongoDeliveryEntity(
        String id,
        String notificationId,
        String payload,
        DeliveryStatus status,
        int retryCount
    ) {
        this.id = id;
        this.notificationId = notificationId;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
