package com.pluralkraft.notification.adapters.inbound.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.pluralkraft.notification.domain.model.RetrySummary;
import com.pluralkraft.notification.ports.in.RetryNotificationUseCase;

/**
 * Scheduled trigger for retrying failed notification deliveries.
 * Uses a fixed delay so runs never overlap within one instance; cross-instance
 * exclusivity is guaranteed by the delivery claim in the repository.
 */
public class RetryNotificationScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(RetryNotificationScheduler.class);

    private final RetryNotificationUseCase retryUseCase;

    /**
     * Constructs a new RetryNotificationScheduler.
     *
     * @param retryUseCase the use case that retries failed deliveries
     */
    public RetryNotificationScheduler(RetryNotificationUseCase retryUseCase) {
        this.retryUseCase = retryUseCase;
    }

    /**
     * Retries failed deliveries on a fixed delay.
     */
    @Scheduled(
        fixedDelayString = "${notification.retry.interval:PT1M}",
        initialDelayString = "${notification.retry.initial-delay:PT30S}"
    )
    public void retryFailedDeliveries() {
        try {
            RetrySummary summary = retryUseCase.retryFailedDeliveries();
            if (summary.claimed() > 0) {
                LOG.info("Retry run completed: {}", summary);
            }
        } catch (RuntimeException e) {
            LOG.error("Retry run failed", e);
        }
    }
}
