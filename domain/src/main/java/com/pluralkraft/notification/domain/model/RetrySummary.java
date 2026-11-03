package com.pluralkraft.notification.domain.model;

/**
 * Outcome of a single retry run over failed deliveries.
 *
 * @param claimed number of deliveries claimed by this run
 * @param succeeded number of deliveries successfully sent on retry
 * @param failed number of deliveries that failed again and remain eligible for retry
 * @param deadLettered number of deliveries published to the dead letter queue
 */
public record RetrySummary(int claimed, int succeeded, int failed, int deadLettered) { }
