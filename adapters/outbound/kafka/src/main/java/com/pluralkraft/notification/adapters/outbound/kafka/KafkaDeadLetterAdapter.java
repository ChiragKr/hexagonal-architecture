package com.pluralkraft.notification.adapters.outbound.kafka;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.kafka.core.KafkaTemplate;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.ports.out.DeadLetterPort;

/**
 * Kafka-based implementation of the DeadLetterPort interface.
 * Publishes undeliverable notifications to the dead letter topic and waits for the broker
 * acknowledgement so callers only mark a delivery as dead-lettered once it is durable.
 */
public class KafkaDeadLetterAdapter implements DeadLetterPort {

    /** Default dead letter topic name. */
    public static final String DEFAULT_TOPIC = "notification.dlq";

    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final String topic;
    private final Duration sendTimeout;

    /**
     * Constructs a new KafkaDeadLetterAdapter.
     *
     * @param kafkaTemplate the Kafka template to use for publishing
     * @param topic the dead letter topic
     * @param sendTimeout maximum time to wait for the broker acknowledgement
     */
    public KafkaDeadLetterAdapter(
        KafkaTemplate<String, Notification> kafkaTemplate,
        String topic,
        Duration sendTimeout
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.sendTimeout = sendTimeout;
    }

    /**
     * Publishes the notification to the dead letter topic, keyed by notification id.
     *
     * @param notification the undeliverable notification
     * @throws IllegalStateException if the publication fails or is not acknowledged in time
     */
    @Override
    public void publish(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        try {
            kafkaTemplate.send(topic, notification.id(), notification)
                .get(sendTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing to " + topic, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Failed to publish notification " + notification.id()
                + " to " + topic, e);
        }
    }
}
