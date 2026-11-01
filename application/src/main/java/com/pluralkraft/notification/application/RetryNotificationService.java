package com.pluralkraft.notification.application;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.domain.model.RetrySummary;
import com.pluralkraft.notification.ports.in.RetryNotificationUseCase;
import com.pluralkraft.notification.ports.out.DeadLetterPort;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import com.pluralkraft.notification.ports.out.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the RetryNotificationUseCase interface.
 * Claims a batch of failed deliveries, retries sending them and publishes those that
 * exhausted their retries to the dead letter queue.
 *
 * <p>Deliveries are claimed through {@link DeliveryRepository#claimFailedDeliveries}, which guarantees
 * that concurrently running application instances never process the same delivery at the same time.
 * The existing delivery is updated in place (rather than going through {@code SendNotificationService},
 * which would create a new delivery record and reset the retry count).
 */
public class RetryNotificationService implements RetryNotificationUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(RetryNotificationService.class);

    private final DeliveryRepository deliveryRepository;
    private final NotificationSender notificationSender;
    private final DeadLetterPort deadLetterPort;
    private final int batchSize;
    private final int maxRetries;
    private final Duration lease;

    /**
     * Constructs a new RetryNotificationService.
     *
     * @param deliveryRepository the repository used to claim and update deliveries
     * @param notificationSender the sender used to re-send notifications
     * @param deadLetterPort the port used to publish notifications that exhausted their retries
     * @param batchSize maximum number of deliveries to claim per run
     * @param maxRetries maximum number of retries before a notification is dead-lettered
     * @param lease how long a claim is held before other instances may re-claim it
     * @throws IllegalArgumentException if any dependency is null or a numeric setting is not positive
     */
    public RetryNotificationService(
        DeliveryRepository deliveryRepository,
        NotificationSender notificationSender,
        DeadLetterPort deadLetterPort,
        int batchSize,
        int maxRetries,
        Duration lease
    ) {
        if (deliveryRepository == null || notificationSender == null || deadLetterPort == null) {
            throw new IllegalArgumentException("Dependencies must not be null");
        }
        if (batchSize <= 0 || maxRetries <= 0) {
            throw new IllegalArgumentException("batchSize and maxRetries must be positive");
        }
        if (lease == null || lease.isZero() || lease.isNegative()) {
            throw new IllegalArgumentException("lease must be positive");
        }
        this.deliveryRepository = deliveryRepository;
        this.notificationSender = notificationSender;
        this.deadLetterPort = deadLetterPort;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
        this.lease = lease;
    }

    @Override
    public RetrySummary retryFailedDeliveries() {
        String claimToken = UUID.randomUUID().toString();
        List<Delivery> claimed = deliveryRepository.claimFailedDeliveries(batchSize, claimToken, lease);

        int succeeded = 0;
        int failed = 0;
        int deadLettered = 0;

        for (Delivery delivery : claimed) {
            DeliveryStatus outcome;
            try {
                outcome = process(delivery, claimToken);
            } catch (RuntimeException e) {
                LOG.error("Unexpected error retrying delivery {}", delivery.id(), e);
                outcome = DeliveryStatus.FAILURE;
            }
            switch (outcome) {
                case SUCCESS -> succeeded++;
                case DEAD_LETTERED -> deadLettered++;
                default -> failed++;
            }
        }

        return new RetrySummary(claimed.size(), succeeded, failed, deadLettered);
    }

    /**
     * Retries a single claimed delivery.
     *
     * @return the resulting status of the delivery
     */
    private DeliveryStatus process(Delivery delivery, String claimToken) {
        Notification notification = new Notification(delivery.notificationId(), delivery.payload());

        // Retries already exhausted (previous DLQ publish failed) or legacy record without payload.
        if (delivery.retryCount() >= maxRetries || delivery.payload() == null) {
            return deadLetter(delivery, notification, delivery.retryCount(), claimToken);
        }

        int retryCount = delivery.retryCount() + 1;
        Receipt receipt = sendSafely(notification);

        if (receipt.success()) {
            return complete(delivery, claimToken, DeliveryStatus.SUCCESS, retryCount);
        }
        if (retryCount < maxRetries) {
            return complete(delivery, claimToken, DeliveryStatus.FAILURE, retryCount);
        }
        return deadLetter(delivery, notification, retryCount, claimToken);
    }

    private Receipt sendSafely(Notification notification) {
        try {
            Receipt receipt = notificationSender.send(notification);
            return receipt != null ? receipt : new Receipt(false, "No receipt returned");
        } catch (RuntimeException e) {
            return new Receipt(false, "ERROR: " + e.getMessage());
        }
    }

    private DeliveryStatus deadLetter(
        Delivery delivery,
        Notification notification,
        int retryCount,
        String claimToken
    ) {
        try {
            deadLetterPort.publish(notification);
        } catch (RuntimeException e) {
            // Keep FAILURE with exhausted retryCount so the next run re-attempts the DLQ publish.
            LOG.error("Failed to publish delivery {} to dead letter queue", delivery.id(), e);
            return complete(delivery, claimToken, DeliveryStatus.FAILURE, retryCount);
        }
        return complete(delivery, claimToken, DeliveryStatus.DEAD_LETTERED, retryCount);
    }

    private DeliveryStatus complete(Delivery delivery, String claimToken, DeliveryStatus status, int retryCount) {
        boolean applied = deliveryRepository.completeRetryAttempt(delivery.id(), claimToken, status, retryCount);
        if (!applied) {
            LOG.warn("Claim on delivery {} expired before the retry result could be recorded", delivery.id());
        }
        return status;
    }
}
