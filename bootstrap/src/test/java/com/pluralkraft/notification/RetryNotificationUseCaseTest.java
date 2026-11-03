package com.pluralkraft.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pluralkraft.notification.application.RetryNotificationService;
import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.domain.model.RetrySummary;
import com.pluralkraft.notification.ports.in.RetryNotificationUseCase;
import com.pluralkraft.notification.ports.out.DeadLetterPort;

/**
 * Tests the Retry Notification use case against the in-memory repository.
 */
class RetryNotificationUseCaseTest {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;
    private static final Duration LEASE = Duration.ofMinutes(5);
    private static final Instant NOW = Instant.parse("2026-11-01T10:00:00Z");

    private InMemoryDeliveryRepository repository;
    private CapturingDeadLetterPort deadLetterPort;
    private Function<Notification, Receipt> senderBehaviour;
    private List<Notification> sent;
    private RetryNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDeliveryRepository(Clock.fixed(NOW, ZoneOffset.UTC));
        deadLetterPort = new CapturingDeadLetterPort();
        sent = new ArrayList<>();
        senderBehaviour = n -> new Receipt(false, "FAILED");
        useCase = new RetryNotificationService(
            repository,
            notification -> {
                sent.add(notification);
                return senderBehaviour.apply(notification);
            },
            deadLetterPort,
            BATCH_SIZE,
            MAX_RETRIES,
            LEASE
        );
    }

    @Test
    void successfulRetryMarksDeliveryAsSuccessAndReleasesClaim() {
        Delivery failed = saveFailed("hello");
        senderBehaviour = n -> new Receipt(true, "SUCCESS");

        RetrySummary summary = useCase.retryFailedDeliveries();

        assertEquals(new RetrySummary(1, 1, 0, 0), summary);
        assertEquals(List.of(new Notification(failed.notificationId(), "hello")), sent);
        Delivery updated = repository.getDelivery(failed.id());
        assertEquals(DeliveryStatus.SUCCESS, updated.status());
        assertEquals(1, updated.retryCount());
        assertFalse(repository.isClaimed(failed.id()));
    }

    @Test
    void deliveryIsDeadLetteredAfterMaxRetries() {
        Delivery failed = saveFailed("payload");

        useCase.retryFailedDeliveries();
        useCase.retryFailedDeliveries();
        assertEquals(DeliveryStatus.FAILURE, repository.getDelivery(failed.id()).status());
        assertEquals(2, repository.getDelivery(failed.id()).retryCount());
        assertTrue(deadLetterPort.published.isEmpty());

        RetrySummary third = useCase.retryFailedDeliveries();

        assertEquals(new RetrySummary(1, 0, 0, 1), third);
        assertEquals(MAX_RETRIES, sent.size());
        Delivery updated = repository.getDelivery(failed.id());
        assertEquals(DeliveryStatus.DEAD_LETTERED, updated.status());
        assertEquals(MAX_RETRIES, updated.retryCount());
        assertEquals(List.of(new Notification(failed.notificationId(), "payload")), deadLetterPort.published);

        // Dead-lettered deliveries are never picked up again.
        assertEquals(new RetrySummary(0, 0, 0, 0), useCase.retryFailedDeliveries());
        assertEquals(MAX_RETRIES, sent.size());
    }

    @Test
    void senderExceptionCountsAsFailedAttempt() {
        Delivery failed = saveFailed("payload");
        senderBehaviour = n -> {
            throw new IllegalStateException("endpoint down");
        };

        RetrySummary summary = useCase.retryFailedDeliveries();

        assertEquals(new RetrySummary(1, 0, 1, 0), summary);
        assertEquals(DeliveryStatus.FAILURE, repository.getDelivery(failed.id()).status());
        assertEquals(1, repository.getDelivery(failed.id()).retryCount());
        assertFalse(repository.isClaimed(failed.id()));
    }

    @Test
    void failedDeadLetterPublishIsRetriedOnNextRunWithoutResending() {
        Delivery exhausted = repository.save(new Delivery(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), "payload",
            DeliveryStatus.FAILURE, MAX_RETRIES - 1));
        deadLetterPort.failNext = true;

        RetrySummary first = useCase.retryFailedDeliveries();

        assertEquals(new RetrySummary(1, 0, 1, 0), first);
        assertEquals(DeliveryStatus.FAILURE, repository.getDelivery(exhausted.id()).status());
        assertEquals(MAX_RETRIES, repository.getDelivery(exhausted.id()).retryCount());
        assertTrue(deadLetterPort.published.isEmpty());

        RetrySummary second = useCase.retryFailedDeliveries();

        assertEquals(new RetrySummary(1, 0, 0, 1), second);
        assertEquals(1, sent.size(), "notification must not be re-sent once retries are exhausted");
        assertEquals(DeliveryStatus.DEAD_LETTERED, repository.getDelivery(exhausted.id()).status());
        assertEquals(1, deadLetterPort.published.size());
    }

    @Test
    void onlyMostRecentFailuresUpToBatchSizeAreClaimed() {
        List<Delivery> failures = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            failures.add(saveFailed("p" + i));
        }
        repository.save(new Delivery(UUID.randomUUID().toString(), "ok", "x", DeliveryStatus.SUCCESS));
        repository.save(new Delivery(UUID.randomUUID().toString(), "dead", "x", DeliveryStatus.DEAD_LETTERED, 3));
        repository.save(new Delivery(UUID.randomUUID().toString(), "pending", "x", DeliveryStatus.PENDING));

        RetrySummary summary = useCase.retryFailedDeliveries();

        assertEquals(BATCH_SIZE, summary.claimed());
        Set<String> expected = new HashSet<>();
        failures.subList(10, 60).forEach(d -> expected.add(d.notificationId()));
        Set<String> retried = new HashSet<>();
        sent.forEach(n -> retried.add(n.id()));
        assertEquals(expected, retried);
    }

    @Test
    void concurrentClaimsNeverOverlap() {
        for (int i = 0; i < 30; i++) {
            saveFailed("p" + i);
        }

        List<Delivery> first = repository.claimFailedDeliveries(20, "instance-a", LEASE);
        List<Delivery> second = repository.claimFailedDeliveries(20, "instance-b", LEASE);

        assertEquals(20, first.size());
        assertEquals(10, second.size());
        Set<String> ids = new HashSet<>();
        first.forEach(d -> ids.add(d.id()));
        second.forEach(d -> assertTrue(ids.add(d.id()), "delivery claimed twice: " + d.id()));
        assertTrue(repository.claimFailedDeliveries(20, "instance-c", LEASE).isEmpty());
    }

    @Test
    void expiredClaimCanBeTakenOverAndStaleOwnerCannotWrite() {
        Delivery failed = saveFailed("payload");
        repository.claimFailedDeliveries(BATCH_SIZE, "crashed-instance", LEASE);

        // Lease still active: nothing to claim.
        assertEquals(new RetrySummary(0, 0, 0, 0), useCase.retryFailedDeliveries());

        // Lease expired: another instance takes over.
        repository.setClock(Clock.fixed(NOW.plus(LEASE).plusSeconds(1), ZoneOffset.UTC));
        senderBehaviour = n -> new Receipt(true, "SUCCESS");
        assertEquals(new RetrySummary(1, 1, 0, 0), useCase.retryFailedDeliveries());

        // The original (stale) owner can no longer overwrite the result.
        assertFalse(repository.completeRetryAttempt(failed.id(), "crashed-instance", DeliveryStatus.FAILURE, 1));
        assertEquals(DeliveryStatus.SUCCESS, repository.getDelivery(failed.id()).status());
    }

    private Delivery saveFailed(String payload) {
        return repository.save(new Delivery(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), payload, DeliveryStatus.FAILURE));
    }

    private static final class CapturingDeadLetterPort implements DeadLetterPort {
        private final List<Notification> published = new ArrayList<>();
        private boolean failNext;

        @Override
        public void publish(Notification notification) {
            if (failNext) {
                failNext = false;
                throw new IllegalStateException("broker unavailable");
            }
            published.add(notification);
        }
    }
}
