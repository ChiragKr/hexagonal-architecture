package com.pluralkraft.notification.adapters.inbound.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pluralkraft.notification.domain.model.Notification;

@RestController
public class PingController {
    
    private static final Logger LOG = LoggerFactory.getLogger(PingController.class);

    /**
     * Pings application and returns random response.
     *
     * @param request the notification request containing details about the notification
     * @return ResponseEntity with either 200 (75%) or 500 (25%) response
     */
    @PostMapping("/ping")
    public ResponseEntity<Void> ping(@RequestBody Notification request) {
        LOG.info("Received ping request with Notificaiton={}", request);
        double rand = Math.random();
        if (rand <= 0.75) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }
}
