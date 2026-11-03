/**
 * Repository interface for managing Delivery entities.
 * This interface defines the contract for persistence operations related to delivery records.
 */
package com.pluralkraft.notification.ports.out;

import java.time.Duration;
import java.util.List;

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

    /**
     * Atomically claims up to {@code limit} of the most recently failed deliveries that are not
     * currently claimed (or whose claim has expired). Safe to call concurrently from multiple
     * application instances: each delivery is claimed by at most one caller.
     *
     * @param limit maximum number of deliveries to claim
     * @param claimToken unique token identifying the claimant
     * @param lease how long the claim remains valid before other instances may re-claim it
     * @return the deliveries claimed with {@code claimToken}
     * @throws IllegalArgumentException if limit is not positive, claimToken is blank or lease is not positive
     */
    List<Delivery> claimFailedDeliveries(int limit, String claimToken, Duration lease);

    /**
     * Records the outcome of a retry attempt and releases the claim. The update is only applied
     * if the delivery is still claimed with {@code claimToken}.
     *
     * @param deliveryId the unique identifier of the delivery
     * @param claimToken the token used to claim the delivery
     * @param status the new delivery status
     * @param retryCount the new retry count
     * @return {@code true} if the update was applied, {@code false} if the claim was lost
     * @throws IllegalArgumentException if deliveryId or claimToken is blank, or status is null
     */
    boolean completeRetryAttempt(String deliveryId, String claimToken, DeliveryStatus status, int retryCount);
}