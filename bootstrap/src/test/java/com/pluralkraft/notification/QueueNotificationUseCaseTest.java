package com.pluralkraft.notification;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.pluralkraft.notification.configuration.AdapterConfig;
import com.pluralkraft.notification.configuration.UseCaseConfig;
import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import com.pluralkraft.notification.ports.out.NotificationSender;

/**
 * Integration test for queue notification use case.
 * 
 * Test validates the complete flow through the system including:
 * - REST API endpoints
 * - Kafka message processing
 * - In-memory persistence
 */
@SpringBootTest(
	classes = {
		AdapterConfig.class,
		UseCaseConfig.class,
		NotificationApplication.class
	}, 
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EmbeddedKafka(
    partitions = 1,
    topics = {
        "notifications"
    },
    brokerProperties = {
        "listeners=EXTERNAL://localhost:0,CONTROLLER://localhost:0",
        "listener.security.protocol.map=EXTERNAL:PLAINTEXT,CONTROLLER:PLAINTEXT",
        "inter.broker.listener.name=EXTERNAL",
		"controller.listener.names=CONTROLLER"
    }
)
@TestPropertySource(
    locations = "classpath:application.yaml",
    properties = {
        "spring.kafka.topic.name=notifications",
        "spring.kafka.consumer.group-id=notification-group-it",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "classpath:/"
    }
)
@AutoConfigureRestTestClient
@DirtiesContext
class QueueNotificationUseCaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTestClient restTemplate;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @MockitoBean
    private NotificationSender notificationSender;

    /**
     * Tests the Queue Notification use case - adding to Kafka queue for async processing.
     * 
     * This validates that:
     * - Notifications can be queued for later processing via Kafka.
     * - Delivery records are properly created and updated in Repository.
     */
    @Test
    void queueNotificationUseCase() throws Exception {
        // Given: A notification request and mock sender behavior
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                "Test message: " + "test@example.com");
        Receipt expectedReceipt = new Receipt(true, "Success");
        when(notificationSender.send(any(Notification.class))).thenReturn(expectedReceipt);
		
        // When & Then: Queuing notification via REST API and verifying response
		restTemplate.post()
				.uri("/api/notifications/queue")
				.contentType(MediaType.APPLICATION_JSON)
				.body(notification)
				.exchange()
				.expectStatus().isAccepted()
                .expectBody().isEmpty();

        // Then: Wait for the async Kafka consumer to process and invoke the sender
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .untilAsserted(() ->
                verify(notificationSender, times(1)).send(any(Notification.class))
            );

        // Then: Check that the delivery was persisted and status updated
        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .until(() -> {
                // Check that a delivery was created with the correct notification ID
                boolean found = false;
                InMemoryDeliveryRepository inMemoryDeliveryRepository = 
                    (InMemoryDeliveryRepository) deliveryRepository;
                for (Delivery delivery : inMemoryDeliveryRepository.getAllDeliveries().values()) {
                    if (delivery.notificationId().equals(notification.id())) {
                        found = true;
                        // Verify the status was updated to DELIVERED
                        assertEquals(DeliveryStatus.SUCCESS, delivery.status());
                        break;
                    }
                }
                return found;
            });
    }
}
