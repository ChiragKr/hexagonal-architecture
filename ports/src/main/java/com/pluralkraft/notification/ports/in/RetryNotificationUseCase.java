/**
 * Use case interface for retrying failed notification deliveries.
 */
package com.pluralkraft.notification.ports.in;

import com.pluralkraft.notification.domain.model.RetrySummary;

/**
 * Defines the contract for retrying failed deliveries and dead-lettering
 * those that exhausted their retries.
 */
public interface RetryNotificationUseCase {
    /**
     * Claims a batch of failed deliveries for this instance and retries them.
     * Deliveries that exceed the maximum number of retries are published to the dead letter queue.
     *
     * @return a summary of the retry run
     */
    RetrySummary retryFailedDeliveries();
}
