package com.pluralkraft.notification.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pluralkraft.notification.adapters.inbound.kafka.KafkaNotificationConsumer;
import com.pluralkraft.notification.ports.in.ProcessNotificationUseCase;

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
}
