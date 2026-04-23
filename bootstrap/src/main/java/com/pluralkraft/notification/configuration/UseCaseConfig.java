/**
 * Configuration class for notification use cases.
 * This class provides Spring Bean definitions for the various notification use cases
 */
package com.pluralkraft.notification.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pluralkraft.notification.application.ProcessNotificationService;
import com.pluralkraft.notification.application.QueueNotificationService;
import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.ports.in.ProcessNotificationUseCase;
import com.pluralkraft.notification.ports.in.QueueNotificationUseCase;
import com.pluralkraft.notification.ports.in.SendNotificationUseCase;
import com.pluralkraft.notification.ports.out.QueueNotificationPort;

/**
 * Configuration class for notification use cases.
 * This class provides Spring Bean definitions for the various notification use cases
 */
@Configuration
public class UseCaseConfig {

    /**
     * Creates and configures the SendNotificationUseCase bean.
     *
     * @return A SendNotificationUseCase implementation that logs notifications and
     *         returns a successful receipt
     * @deprecated This is a temporary implementation. It should be replaced with
     *             a proper SendNotificationService once outbound ports are implemented.
     */
    @Bean
    SendNotificationUseCase sendNotificationUseCase() {
        // TODO: Return SendNotificationService instead, after outbound ports implemented
        return new SendNotificationUseCase() {

            private static final Logger LOG = LoggerFactory.getLogger("NoOpSendNotification");

            @Override
            public Receipt send(Notification notification) {
                LOG.info("Notification {}", notification);
                return new Receipt(true, "SUCCESS");
            }

        };
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
     * Creates and configures the ProcessNotificationUseCase bean.
     *
     * @param sender The SendNotificationUseCase dependency to be used by the service
     * @return A ProcessNotificationUseCase implementation using the provided sender
     */
    @Bean
    ProcessNotificationUseCase processNotificationUseCase(SendNotificationUseCase sender) {
        return new ProcessNotificationService(sender);
    }
}