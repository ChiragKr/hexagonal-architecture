/**
 * Repository interface for managing Delivery entities.
 * This interface defines the contract for persistence operations related to delivery records.
 */
package com.pluralkraft.notification.ports.out;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;

/**
 * Defines the contract for saving and updating delivery status operations.
 */
public interface DeliveryRepository {
    /**
     * Saves a delivery entity to the persistence store.
     *
     * @param delivery the delivery object to be saved
     * @return the saved delivery entity with any generated identifiers populated
     * @throws IllegalArgumentException if the delivery parameter is null
     */
    Delivery save(Delivery delivery);

    /**
     * Updates the status of a delivery identified by its ID.
     *
     * @param deliveryId the unique identifier of the delivery to update
     * @param status the new status to set for the delivery
     * @throws IllegalArgumentException if deliveryId is null or empty, or status is null
     */
    void updateStatus(String deliveryId, DeliveryStatus status);
}