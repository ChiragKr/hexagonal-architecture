/**
 * REST controller for handling notification operations.
 * This controller provides endpoints for operating notifications through the system.
 */
package com.pluralkraft.notification.adapters.inbound.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.ports.in.QueueNotificationUseCase;
import com.pluralkraft.notification.ports.in.SendNotificationUseCase;

/**
 * Controller for handling notification operations.
 * Provides REST endpoints for operating notifications using the appropriate use cases.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    /**
     * The use case for sending notifications.
     */
    private final SendNotificationUseCase sendUseCase;

    /**
     * The use case for queuing notifications.
     */
    private final QueueNotificationUseCase queueUseCase;

    /**
     * Constructs a new NotificationController with the specified use cases.
     *
     * @param sendUseCase the use case to be used for sending notifications
     * @param queueUseCase the use case to be used for queuing notifications
     */
    @Autowired
    NotificationController(
        SendNotificationUseCase sendUseCase,
        QueueNotificationUseCase queueUseCase
    ) {
        this.sendUseCase = sendUseCase;
        this.queueUseCase = queueUseCase;
    }

    /**
     * Sends a notification based on the provided request.
     *
     * @param request the notification request containing details about the notification to send
     * @return ResponseEntity with the receipt of the sent notification
     */
    @PostMapping("/send")
    public ResponseEntity<Receipt> send(@RequestBody Notification request) {
        return ResponseEntity.ok(sendUseCase.send(request));
    }

    /**
     * Queues a notification for later processing.
     *
     * @param request the notification request containing details about the notification to queue
     * @return ResponseEntity with accepted status indicating successful queuing
     */
    @PostMapping("/queue")
    public ResponseEntity<Void> queue(@RequestBody Notification request) {
        queueUseCase.queue(request);
        return ResponseEntity.accepted().build();
    }
}
