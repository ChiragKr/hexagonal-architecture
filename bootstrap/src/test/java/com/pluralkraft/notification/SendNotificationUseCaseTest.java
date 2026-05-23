package com.pluralkraft.notification;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import com.pluralkraft.notification.ports.out.NotificationSender;

@AutoConfigureRestTestClient
class SendNotificationUseCaseTest extends NotificationApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTestClient restTemplate;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @MockitoBean
    private NotificationSender notificationSender;

    /**
     * Tests the Send Notification use case - immediate delivery through REST endpoint.
     * This validates that notifications can be sent immediately and the receipt is
     * properly returned.
     */
    @Test
    void sendNotificationUseCase() throws Exception {
        // Given: A notification request and in-memory delivery repository
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                "Test message: " + "test@example.com");
        Receipt expectedReceipt = new Receipt(true, "Success");

        // Mock the notification sender behavior
        when(notificationSender.send(any(Notification.class))).thenReturn(expectedReceipt);

		// When & Then: Sending notification via REST API and verifying response
		restTemplate.post()
				.uri("/api/notifications/send")
				.contentType(MediaType.APPLICATION_JSON)
				.body(notification)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Receipt.class)
				.value(receipt -> {
					assertNotNull(receipt);
					assertTrue(receipt.success());
				});

        // Verify that the notification sender was called
        verify(notificationSender, times(1)).send(any(Notification.class));

        // Use awaitility to check that the delivery was persisted and status updated
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
