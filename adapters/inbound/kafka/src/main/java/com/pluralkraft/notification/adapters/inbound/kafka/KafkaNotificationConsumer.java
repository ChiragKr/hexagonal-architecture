package com.pluralkraft.notification.adapters.inbound.kafka;

import org.springframework.kafka.annotation.KafkaListener;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.ports.in.ProcessNotificationUseCase;

/**
 * Kafka consumer for handling notification messages.
 * This adapter listens to the "notifications" topic and processes incoming
 * notification messages using the process notification use case.
 */
public class KafkaNotificationConsumer {

    /**
     * The use case responsible for processing notifications.
     */
    private final ProcessNotificationUseCase processUseCase;

    /**
     * Constructs a new KafkaNotificationConsumer with the specified use case.
     *
     * @param processUseCase the use case to process notifications
     */
    public KafkaNotificationConsumer(
        ProcessNotificationUseCase processUseCase
    ) {
        this.processUseCase = processUseCase;
    }

    /**
     * Consumes a notification message from the Kafka "notifications" topic.
     * This method is automatically invoked when a message is received on the topic.
     *
     * @param notification the notification to process
     */
    @KafkaListener(topics = "notifications")
    public void consume(Notification notification) {
        processUseCase.process(notification);
    }
}
