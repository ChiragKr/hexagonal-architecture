package com.pluralkraft.notification.domain.model;

/**
 * Receipt from sender attempting to send Notification.
 */
public record Receipt (boolean success, String response) { }