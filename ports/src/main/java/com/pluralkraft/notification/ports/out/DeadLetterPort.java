/**
 * Interface for publishing undeliverable notifications.
 */
package com.pluralkraft.notification.ports.out;

import com.pluralkraft.notification.domain.model.Notification;

/**
 * Defines the contract for publishing notifications that could not be delivered.
 */
public interface DeadLetterPort {
    /**
     * Publishes a notification to the dead letter queue. Returns only once the
     * publication has been acknowledged.
     *
     * @param notification the undeliverable notification
     * @throws RuntimeException if the notification could not be published
     */
    void publish(Notification notification);
}
