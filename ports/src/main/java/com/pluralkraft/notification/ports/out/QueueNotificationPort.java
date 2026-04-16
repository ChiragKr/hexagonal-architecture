/**
 * Interface for queue-based notification handling.
 * This interface defines the contract for enqueueing and dequeueing notifications in a queue system.
 */
package com.pluralkraft.notification.ports.out;

import com.pluralkraft.notification.domain.model.Notification;

/**
 * Defines the contract for queue-based notification operations.
 */
public interface QueueNotificationPort {
    /**
     * Adds a notification to the queue.
     *
     * @param notification the notification to be added to the queue
     * @throws IllegalArgumentException if the notification parameter is null
     */
    void enqueue(Notification notification);

    /**
     * Removes and returns the next notification from the queue.
     *
     * @return the next notification in the queue
     * @throws IllegalStateException if the queue is empty
     */
    Notification dequeue();
}