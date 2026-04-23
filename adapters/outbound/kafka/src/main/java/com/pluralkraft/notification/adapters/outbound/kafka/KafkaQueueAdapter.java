package com.pluralkraft.notification.adapters.outbound.kafka;

import org.springframework.kafka.core.KafkaTemplate;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.ports.out.QueueNotificationPort;

/**
 * Kafka-based implementation of the QueueNotificationPort interface.
 * This adapter is responsible for sending notifications to a Kafka topic
 * for asynchronous processing.
 */
public class KafkaQueueAdapter implements QueueNotificationPort {

    private final KafkaTemplate<String, Notification> kafkaTemplate;

    /**
     * Constructs a new KafkaQueueAdapter with the specified Kafka template.
     *
     * @param kafkaTemplate the Kafka template to use for sending notifications
     */
    public KafkaQueueAdapter(
        KafkaTemplate<String, Notification> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Enqueues a notification to the Kafka topic.
     *
     * @param notification the notification to enqueue
     */
    @Override
    public void enqueue(Notification notification) {
        kafkaTemplate.send("notifications", notification);
    }

    /**
     * {@inheritDoc}
     *
     * Note: This operation is not supported in the Kafka adapter.
     * Kafka is designed for publish-subscribe patterns and does not
     * support traditional queue dequeue operations.
     *
     * @throws UnsupportedOperationException always thrown as this operation is not supported
     */
    @Override
    public Notification dequeue() {
        throw new UnsupportedOperationException();
    }
}
