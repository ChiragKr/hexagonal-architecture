package com.pluralkraft.notification.application;

import java.util.UUID;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.ports.in.SendNotificationUseCase;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import com.pluralkraft.notification.ports.out.NotificationSender;

/**
 * Implementation of the SendNotificationUseCase interface.
 * This service is responsible for sending notifications and managing delivery tracking.
 */
public class SendNotificationService implements SendNotificationUseCase {

    /**
     * The delivery repository used to persist delivery information.
     */
    private final DeliveryRepository deliveryRepository;

    /**
     * The notification sender used to actually send notifications.
     */
    private final NotificationSender notificationSender;

    /**
     * Constructs a new SendNotificationService with the specified dependencies.
     *
     * @param repo the delivery repository to use for persisting delivery information
     * @param sender the notification sender to use for sending notifications
     */
    public SendNotificationService(
        DeliveryRepository repo,
        NotificationSender sender
    ) {
        this.deliveryRepository = repo;
        this.notificationSender = sender;
    }

    /**
     * Sends a notification and returns a receipt confirming delivery.
     *
     * @param notification the notification to be sent
     * @return a receipt confirming the delivery of the notification
     * @throws IllegalArgumentException if the notification parameter is null
     */
    @Override
    public Receipt send(Notification notification) {
        String deliveryId = UUID.randomUUID().toString();

        Delivery delivery = new Delivery(
            deliveryId,
            notification.id(),
            DeliveryStatus.PENDING);

        delivery = deliveryRepository.save(delivery);

        Receipt receipt = notificationSender.send(notification);

        DeliveryStatus status = receipt.success()
                ? DeliveryStatus.SUCCESS
                : DeliveryStatus.FAILURE;

        deliveryRepository.updateStatus(delivery.id(), status);

        return receipt;
    }
}
