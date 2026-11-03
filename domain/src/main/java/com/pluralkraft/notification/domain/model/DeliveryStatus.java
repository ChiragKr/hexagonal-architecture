package com.pluralkraft.notification.domain.model;

/**
 * Delivery status of an attempt.
 */
public enum DeliveryStatus {
    PENDING, SUCCESS, FAILURE,
    /** Retries exhausted; notification has been published to the dead letter queue. */
    DEAD_LETTERED
}
