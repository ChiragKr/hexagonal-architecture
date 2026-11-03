package com.pluralkraft.notification.domain.model;

/**
 * Delivery attempt to send a Notification.
 *
 * @param id unique identifier of the delivery
 * @param notificationId identifier of the notification being delivered
 * @param payload notification payload, persisted so the notification can be retried
 * @param status current delivery status
 * @param retryCount number of retries already performed for this delivery
 */
public record Delivery(
    String id,
    String notificationId,
    String payload,
    DeliveryStatus status,
    int retryCount
) {

    /**
     * Creates a delivery that has not been retried yet.
     */
    public Delivery(String id, String notificationId, String payload, DeliveryStatus status) {
        this(id, notificationId, payload, status, 0);
    }
}
