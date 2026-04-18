package com.pluralkraft.notification.application;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.ports.in.ProcessNotificationUseCase;
import com.pluralkraft.notification.ports.in.SendNotificationUseCase;

/**
 * Implementation of the ProcessNotificationUseCase interface.
 * This service is responsible for processing notifications by delegating to the
 * SendNotificationUseCase for actual sending operations.
 */
public class ProcessNotificationService implements ProcessNotificationUseCase {

    /**
     * The send notification use case used to send notifications.
     */
    private final SendNotificationUseCase sender;

    /**
     * Constructs a new ProcessNotificationService with the specified send notification use case.
     *
     * @param sender the send notification use case to use for sending notifications
     */
    public ProcessNotificationService(SendNotificationUseCase sender) {
        this.sender = sender;
    }

    /**
     * Processes a notification asynchronously.
     *
     * @param notification the notification to be processed
     * @throws IllegalArgumentException if the notification parameter is null
     */
    @Override
    public void process(Notification notification) {
        sender.send(notification);
    }
}
