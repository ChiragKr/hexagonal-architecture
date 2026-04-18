package com.pluralkraft.notification.application;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.ports.in.QueueNotificationUseCase;
import com.pluralkraft.notification.ports.out.QueueNotificationPort;

/**
 * Implementation of the QueueNotificationUseCase interface.
 * This service is responsible for queuing notifications by delegating to the
 * QueueNotificationPort for actual enqueueing operations.
 */
public class QueueNotificationService implements QueueNotificationUseCase {

    /**
     * The queue notification port used to enqueue notifications.
     */
    private final QueueNotificationPort queue;

    /**
     * Constructs a new QueueNotificationService with the specified queue notification port.
     *
     * @param queue the queue notification port to use for enqueueing notifications
     */
    public QueueNotificationService(QueueNotificationPort queue) {
        this.queue = queue;
    }

    /**
     * Queues the specified notification for processing.
     *
     * @param notification the notification to queue
     * @throws IllegalArgumentException if the notification parameter is null
     */
    @Override
    public void queue(Notification notification) {
        queue.enqueue(notification);
    }
}
