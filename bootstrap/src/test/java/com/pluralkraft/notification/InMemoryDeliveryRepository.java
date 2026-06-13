package com.pluralkraft.notification;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.ports.out.DeliveryRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * In-memory implementation of DeliveryRepository for testing purposes.
 * This allows testing the complete flow without mocking the repository.
 */
public class InMemoryDeliveryRepository implements DeliveryRepository {

    private final Map<String, Delivery> deliveries = new ConcurrentHashMap<>();

    @Override
    public Delivery save(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Delivery cannot be null");
        }

        // If the delivery doesn't have an ID, generate one
        String deliveryId = delivery.id() != null ? delivery.id() : UUID.randomUUID().toString();

        Delivery savedDelivery = new Delivery(
            deliveryId,
            delivery.notificationId(),
            delivery.status()
        );

        deliveries.put(deliveryId, savedDelivery);
        return savedDelivery;
    }

    @Override
    public void updateStatus(String deliveryId, DeliveryStatus status) {
        if (deliveryId == null || deliveryId.isEmpty()) {
            throw new IllegalArgumentException("Delivery ID cannot be null or empty");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        Delivery existingDelivery = deliveries.get(deliveryId);
        if (existingDelivery != null) {
            Delivery updatedDelivery = new Delivery(
                existingDelivery.id(),
                existingDelivery.notificationId(),
                status
            );
            deliveries.put(deliveryId, updatedDelivery);
        }
    }

    /**
     * Gets a delivery by ID
     */
    public Delivery getDelivery(String deliveryId) {
        return deliveries.get(deliveryId);
    }

    /**
     * Gets all deliveries
     */
    public Map<String, Delivery> getAllDeliveries() {
        return new ConcurrentHashMap<>(deliveries);
    }
}