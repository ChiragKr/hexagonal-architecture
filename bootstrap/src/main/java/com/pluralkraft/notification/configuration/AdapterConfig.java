package com.pluralkraft.notification.configuration;

import java.time.Duration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pluralkraft.notification.adapters.inbound.kafka.KafkaNotificationConsumer;
import com.pluralkraft.notification.adapters.inbound.scheduler.RetryNotificationScheduler;
import com.pluralkraft.notification.adapters.outbound.kafka.KafkaDeadLetterAdapter;
import com.pluralkraft.notification.adapters.outbound.kafka.KafkaQueueAdapter;
import com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryRepository;
import com.pluralkraft.notification.adapters.outbound.web.WebNotificationSender;
import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.ports.in.RetryNotificationUseCase;
import com.pluralkraft.notification.ports.in.SendNotificationUseCase;
import com.pluralkraft.notification.ports.out.DeadLetterPort;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import com.pluralkraft.notification.ports.out.NotificationSender;
import com.pluralkraft.notification.ports.out.QueueNotificationPort;

/**
 * Configuration class for notification adapters.
 * This class provides Spring Bean definitions for the various inbound/outbound adapters
 */
@Configuration
@EnableScheduling
public class AdapterConfig {

    /**
     * Creates and configures the KafkaNotificationConsumer bean.
     * This bean listens to the "notifications" Kafka topic and processes
     * incoming notification messages using the provided use case.
     *
     * @param sendNotificationUseCase the use case to send notifications
     * @return a configured KafkaNotificationConsumer instance
     */
    @Bean
    KafkaNotificationConsumer kafkaNotificationConsumer(
        SendNotificationUseCase sendNotificationUseCase
    ) {
        return new KafkaNotificationConsumer(sendNotificationUseCase);
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

    /**
     * Declares the dead letter topic so it is created on startup if missing.
     *
     * @param topic the dead letter topic name
     * @return the topic definition
     */
    @Bean
    NewTopic deadLetterTopic(
        @Value("${notification.retry.dlq-topic:notification.dlq}") String topic
    ) {
        return TopicBuilder.name(topic).build();
    }

    /**
     * Creates the Kafka-based dead letter adapter bean.
     *
     * @param kafkaTemplate the Kafka template to use for publishing
     * @param topic the dead letter topic name
     * @param sendTimeout maximum time to wait for the broker acknowledgement
     * @return a configured DeadLetterPort instance
     */
    @Bean
    DeadLetterPort kafkaDeadLetterAdapter(
        KafkaTemplate<String, Notification> kafkaTemplate,
        @Value("${notification.retry.dlq-topic:notification.dlq}") String topic,
        @Value("${notification.retry.dlq-send-timeout:PT10S}") Duration sendTimeout
    ) {
        return new KafkaDeadLetterAdapter(kafkaTemplate, topic, sendTimeout);
    }

    /**
     * Creates the scheduler that periodically retries failed deliveries.
     *
     * @param retryNotificationUseCase the use case that retries failed deliveries
     * @return a configured RetryNotificationScheduler instance
     */
    @Bean
    RetryNotificationScheduler retryNotificationScheduler(
        RetryNotificationUseCase retryNotificationUseCase
    ) {
        return new RetryNotificationScheduler(retryNotificationUseCase);
    }
}
