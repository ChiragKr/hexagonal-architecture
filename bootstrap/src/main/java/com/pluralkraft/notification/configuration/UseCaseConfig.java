/**
 * Configuration class for notification use cases.
 * This class provides Spring Bean definitions for the various notification use cases
 */
package com.pluralkraft.notification.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pluralkraft.notification.application.QueueNotificationService;
import com.pluralkraft.notification.application.RetryNotificationService;
import com.pluralkraft.notification.application.SendNotificationService;
import com.pluralkraft.notification.ports.in.QueueNotificationUseCase;
import com.pluralkraft.notification.ports.in.RetryNotificationUseCase;
import com.pluralkraft.notification.ports.in.SendNotificationUseCase;
import com.pluralkraft.notification.ports.out.DeadLetterPort;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import com.pluralkraft.notification.ports.out.NotificationSender;
import com.pluralkraft.notification.ports.out.QueueNotificationPort;

/**
 * Configuration class for notification use cases.
 * This class provides Spring Bean definitions for the various notification use cases
 */
@Configuration
public class UseCaseConfig {

    /**
     * Creates and configures the SendNotificationUseCase bean.
     * This bean provides notification sending functionality by delegating to
     * the specified DeliveryRepository and NotificationSender implementations.
     *
     * @param deliveryRepository the repository for persisting delivery records
     * @param notificationSender the sender for actually delivering notifications
     * @return A SendNotificationUseCase implementation that sends notifications
     *         and persists delivery information
     */
    @Bean
    SendNotificationUseCase sendNotificationUseCase(
        DeliveryRepository deliveryRepository,
        NotificationSender notificationSender
    ) {
        return new SendNotificationService(deliveryRepository, notificationSender);
    }

    /**
     * Creates and configures the QueueNotificationUseCase bean.
     * This bean provides queue-based notification processing functionality
     * by delegating to the provided QueueNotificationPort implementation.
     *
     * @param queue The QueueNotificationPort dependency for queue operations
     * @return A QueueNotificationUseCase implementation that processes notifications
     *         through the provided queue port
     */
    @Bean
    QueueNotificationUseCase queueNotificationUseCase(QueueNotificationPort queue) {
        return new QueueNotificationService(queue);
    }

    /**
     * Creates and configures the RetryNotificationUseCase bean.
     *
     * @param deliveryRepository the repository used to claim and update deliveries
     * @param notificationSender the sender used to re-send notifications
     * @param deadLetterPort the port used to publish notifications that exhausted their retries
     * @param batchSize maximum number of deliveries claimed per run
     * @param maxRetries maximum number of retries before dead-lettering
     * @param lease how long a claim is held before other instances may re-claim it
     * @return a RetryNotificationUseCase implementation
     */
    @Bean
    RetryNotificationUseCase retryNotificationUseCase(
        DeliveryRepository deliveryRepository,
        NotificationSender notificationSender,
        DeadLetterPort deadLetterPort,
        @Value("${notification.retry.batch-size:50}") int batchSize,
        @Value("${notification.retry.max-retries:3}") int maxRetries,
        @Value("${notification.retry.lease:PT5M}") Duration lease
    ) {
        return new RetryNotificationService(
            deliveryRepository, notificationSender, deadLetterPort, batchSize, maxRetries, lease);
    }
}