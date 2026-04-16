package com.pluralkraft.notification.domain.model;

/**
 * Delivery attempt to send a Notification.
 */
public record Delivery (String id, String notificationId, DeliveryStatus status) { }