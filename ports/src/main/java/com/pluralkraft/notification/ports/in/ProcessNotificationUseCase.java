/**
 * Use case interface for processing notifications.
 * This interface defines the contract for handling asynchronous notification processing.
 */
package com.pluralkraft.notification.ports.in;

import com.pluralkraft.notification.domain.model.Notification;

/**
 * Defines the contract for processing notification use cases.
 */
public interface ProcessNotificationUseCase {
    /**
     * Processes a notification asynchronously.
     *
     * @param notification the notification to be processed
     * @throws IllegalArgumentException if the notification parameter is null
     */
    void process(Notification notification);
}