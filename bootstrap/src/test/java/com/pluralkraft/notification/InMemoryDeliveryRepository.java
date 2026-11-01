package com.pluralkraft.notification;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.ports.out.DeliveryRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of DeliveryRepository for testing purposes.
 * This allows testing the complete flow without mocking the repository.
 * Claim semantics mirror the MongoDB implementation (claim token + lease).
 */
public class InMemoryDeliveryRepository implements DeliveryRepository {

    /**
     * Stored delivery with its claim metadata. {@code sequence} orders by last update.
     */
    private record Entry(Delivery delivery, String lockedBy, Instant lockedUntil, long sequence) { }

    private final Map<String, Entry> deliveries = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong();
    private Clock clock;

    public InMemoryDeliveryRepository() {
        this(Clock.systemUTC());
    }

    public InMemoryDeliveryRepository(Clock clock) {
        this.clock = clock;
    }

    /**
     * Replaces the clock used to evaluate claim leases.
     */
    public synchronized void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public synchronized Delivery save(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Delivery cannot be null");
        }

        // If the delivery doesn't have an ID, generate one
        String deliveryId = delivery.id() != null ? delivery.id() : UUID.randomUUID().toString();

        Delivery savedDelivery = new Delivery(
            deliveryId,
            delivery.notificationId(),
            delivery.payload(),
            delivery.status(),
            delivery.retryCount()
        );

        deliveries.put(deliveryId, new Entry(savedDelivery, null, null, sequence.incrementAndGet()));
        return savedDelivery;
    }

    @Override
    public synchronized void updateStatus(String deliveryId, DeliveryStatus status) {
        if (deliveryId == null || deliveryId.isEmpty()) {
            throw new IllegalArgumentException("Delivery ID cannot be null or empty");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        Entry existing = deliveries.get(deliveryId);
        if (existing != null) {
            Delivery d = existing.delivery();
            Delivery updated = new Delivery(d.id(), d.notificationId(), d.payload(), status, d.retryCount());
            deliveries.put(deliveryId,
                new Entry(updated, existing.lockedBy(), existing.lockedUntil(), sequence.incrementAndGet()));
        }
    }

    @Override
    public synchronized List<Delivery> claimFailedDeliveries(int limit, String claimToken, Duration lease) {
        if (limit <= 0 || claimToken == null || claimToken.isBlank()
                || lease == null || lease.isZero() || lease.isNegative()) {
            throw new IllegalArgumentException("Invalid claim arguments");
        }

        Instant now = clock.instant();
        List<Entry> claimable = deliveries.values().stream()
            .filter(e -> e.delivery().status() == DeliveryStatus.FAILURE)
            .filter(e -> e.lockedUntil() == null || e.lockedUntil().isBefore(now))
            .sorted(Comparator.comparingLong(Entry::sequence).reversed())
            .limit(limit)
            .toList();

        claimable.forEach(e -> deliveries.put(e.delivery().id(),
            new Entry(e.delivery(), claimToken, now.plus(lease), e.sequence())));

        return claimable.stream().map(Entry::delivery).toList();
    }

    @Override
    public synchronized boolean completeRetryAttempt(
        String deliveryId,
        String claimToken,
        DeliveryStatus status,
        int retryCount
    ) {
        if (deliveryId == null || deliveryId.isBlank() || claimToken == null || claimToken.isBlank()
                || status == null) {
            throw new IllegalArgumentException("Invalid completion arguments");
        }

        Entry existing = deliveries.get(deliveryId);
        if (existing == null || !claimToken.equals(existing.lockedBy())) {
            return false;
        }
        Delivery d = existing.delivery();
        Delivery updated = new Delivery(d.id(), d.notificationId(), d.payload(), status, retryCount);
        deliveries.put(deliveryId, new Entry(updated, null, null, sequence.incrementAndGet()));
        return true;
    }

    /**
     * Gets a delivery by ID
     */
    public synchronized Delivery getDelivery(String deliveryId) {
        Entry entry = deliveries.get(deliveryId);
        return entry != null ? entry.delivery() : null;
    }

    /**
     * Returns whether the delivery currently holds a claim.
     */
    public synchronized boolean isClaimed(String deliveryId) {
        Entry entry = deliveries.get(deliveryId);
        return entry != null && entry.lockedBy() != null;
    }

    /**
     * Gets all deliveries
     */
    public synchronized Map<String, Delivery> getAllDeliveries() {
        Map<String, Delivery> copy = new HashMap<>();
        deliveries.forEach((id, entry) -> copy.put(id, entry.delivery()));
        return copy;
    }
}
