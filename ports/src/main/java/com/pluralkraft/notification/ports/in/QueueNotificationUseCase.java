/**
 * Use case interface for queuing notifications.
 * This interface defines the contract for adding notifications to a queue system.
 */
package com.pluralkraft.notification.ports.in;

import com.pluralkraft.notification.domain.model.Notification;

/**
 * Defines the contract for queueing notification use cases.
 */
public interface QueueNotificationUseCase {
    /**
     * Adds a notification to the queue for processing later.
     *
     * @param notification the notification to be queued
     * @throws IllegalArgumentException if the notification parameter is null
     */
    void queue(Notification notification);
}