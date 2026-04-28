package com.pluralkraft.notification.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import com.pluralkraft.notification.adapters.inbound.kafka.KafkaNotificationConsumer;
import com.pluralkraft.notification.adapters.outbound.kafka.KafkaQueueAdapter;
import com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryRepository;
import com.pluralkraft.notification.adapters.outbound.web.WebNotificationSender;
import com.pluralkraft.notification.domain.model.Notification;
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

    /**
     * Creates and configures the MongoDB-based delivery repository bean.
     * This bean provides persistence for delivery records using MongoDB as the underlying data store.
     *
     * @param mongoTemplate the Spring Data MongoDB template to use for database operations
     * @return a configured MongoDeliveryRepository instance
     */
    @Bean
    DeliveryRepository mongoDeliveryRepository(
        MongoTemplate mongoTemplate
    ) {
        return new MongoDeliveryRepository(mongoTemplate);
    }

    /**
     * Creates and configures a web-based notification sender bean.
     * This bean makes POST requests to a configurable endpoint at /ping path.
     *
     * @param webEndpointUrl the base URL to send notifications to
     * @return a configured NotificationSender instance that sends notifications via HTTP POST
     */
    @Bean
    NotificationSender webNotificationSender(
        @Value("${notification.web.endpoint:http://localhost:8080}") String webEndpointUrl
    ) {
        return new WebNotificationSender(webEndpointUrl);
    }
}
