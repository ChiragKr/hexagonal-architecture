package com.pluralkraft.notification.adapters.inbound.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.ports.in.SendNotificationUseCase;

/**
 * Kafka consumer for handling notification messages.
 * This adapter listens to the "notifications" topic and processes incoming
 * notification messages using the send notification use case.
 */
public class KafkaNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaNotificationConsumer.class);

    /**
     * The use case responsible for sending notifications.
     */
    private final SendNotificationUseCase sendUseCase;

    /**
     * Constructs a new KafkaNotificationConsumer with the specified use case.
     *
     * @param sendUseCase the use case to send notifications
     */
    public KafkaNotificationConsumer(
        SendNotificationUseCase sendUseCase
    ) {
        this.sendUseCase = sendUseCase;
    }

    /**
     * Consumes a notification message from the Kafka "notifications" topic.
     * This method is automatically invoked when a message is received on the topic.
     *
     * @param notification the notification to process
     */
    @KafkaListener(topics = "notifications")
    public void consume(Notification notification) {
        Receipt receipt = sendUseCase.send(notification);
        log.info("Notification sent: {}", receipt);
    }
}
