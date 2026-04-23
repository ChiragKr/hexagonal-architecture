package com.pluralkraft.notification.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import com.pluralkraft.notification.adapters.inbound.kafka.KafkaNotificationConsumer;
import com.pluralkraft.notification.adapters.outbound.kafka.KafkaQueueAdapter;
import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.ports.in.ProcessNotificationUseCase;
import com.pluralkraft.notification.ports.out.QueueNotificationPort;

/**
 * Configuration class for notification adapters.
 * This class provides Spring Bean definitions for the various inbound/outbound adapters
 */
@Configuration
public class AdapterConfig {

    /**
     * Creates and configures the KafkaNotificationConsumer bean.
     * This bean listens to the "notifications" Kafka topic and processes
     * incoming notification messages using the provided use case.
     *
     * @param processNotificationUseCase the use case to process notifications
     * @return a configured KafkaNotificationConsumer instance
     */
    @Bean
    KafkaNotificationConsumer kafkaNotificationConsumer(
        ProcessNotificationUseCase processNotificationUseCase
    ) {
        return new KafkaNotificationConsumer(processNotificationUseCase);
    }

    /**
     * Creates and configures the Kafka-based queue adapter bean.
     * This bean provides an implementation of QueueNotificationPort that sends
     * notifications to a Kafka topic for asynchronous processing.
     *
     * @param kafkaTemplate the Kafka template to use for sending notifications
     * @return a configured KafkaQueueAdapter instance
     */
    @Bean
    QueueNotificationPort kafkaQueueAdapter(
        KafkaTemplate<String, Notification> kafkaTemplate
    ) {
        return new KafkaQueueAdapter(kafkaTemplate);
    }
}
