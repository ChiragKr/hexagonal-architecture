/**
 * Interface for sending notifications.
 * This interface defines the contract for sending notification messages and returning receipts.
 */
package com.pluralkraft.notification.ports.out;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;

/**
 * Defines the contract for sending notifications and returning delivery receipts.
 */
public interface NotificationSender {
    /**
     * Sends a notification and returns a receipt confirming delivery.
     *
     * @param notification the notification to be sent
     * @return a receipt confirming the delivery of the notification
     * @throws IllegalArgumentException if the notification parameter is null
     */
    Receipt send(Notification notification);
}