package com.pluralkraft.notification.adapters.outbound.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.ports.out.NotificationSender;

/**
 * Web-based implementation of the NotificationSender interface.
 * This adapter makes POST requests to a configurable endpoint at /ping path.
 */
public class WebNotificationSender implements NotificationSender {

    private static final Logger LOG = LoggerFactory.getLogger(WebNotificationSender.class);
    private static final String DEFAULT_ENDPOINT_PATH = "/ping";

    private final RestTemplate restTemplate;
    private final String endpointUrl;

    /**
     * Constructs a new WebNotificationSender with the specified endpoint URL.
     *
     * @param endpointUrl the base URL to send notifications to
     */
    public WebNotificationSender(String endpointUrl) {
        this.endpointUrl = endpointUrl;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends a notification by making a POST request to the configured endpoint.
     *
     * @param notification the notification to be sent
     * @return a receipt confirming the delivery of the notification
     */
    @Override
    public Receipt send(Notification notification) {
        try {
            LOG.info("Sending notification to web endpoint: {}", endpointUrl + DEFAULT_ENDPOINT_PATH);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            // Create request entity with notification data
            HttpEntity<Notification> request = new HttpEntity<>(notification, headers);

            // Make the POST request to /ping endpoint
            ResponseEntity<String> response = restTemplate.exchange(
                endpointUrl + DEFAULT_ENDPOINT_PATH,
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                LOG.info("Notification sent successfully to: {}", endpointUrl + DEFAULT_ENDPOINT_PATH);
                return new Receipt(true, "SUCCESS");
            } else {
                LOG.warn("Failed to send notification. Status code: {}",
                    response.getStatusCode());
                return new Receipt(false, "FAILED");
            }
        } catch (Exception e) {
            LOG.error("Error sending notification to web endpoint: {}", endpointUrl, e);
            return new Receipt(false, "ERROR: " + e.getMessage());
        }
    }
}