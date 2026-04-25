package com.pluralkraft.notification.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import com.pluralkraft.notification.adapters.inbound.kafka.KafkaNotificationConsumer;
import com.pluralkraft.notification.adapters.outbound.kafka.KafkaQueueAdapter;
import com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryRepository;
import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.ports.in.ProcessNotificationUseCase;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import com.pluralkraft.notification.ports.out.NotificationSender;
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

    @Bean
    DeliveryRepository mongoDeliveryRepository(
        MongoTemplate mongoTemplate
    ) {
        return new MongoDeliveryRepository(mongoTemplate);
    }

    @Bean
    NotificationSender noOpNotificationSender() {
        return new NotificationSender() {

            private static final Logger LOG = LoggerFactory.getLogger("NoOpNotificationSender");

            @Override
            public Receipt send(Notification notification) {
                LOG.info("Notification {}", notification);
                return new Receipt(true, "SUCCESS");
            }

        };
    }
}
