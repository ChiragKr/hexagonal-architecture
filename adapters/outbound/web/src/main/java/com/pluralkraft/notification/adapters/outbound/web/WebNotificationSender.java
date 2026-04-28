package com.pluralkraft.notification.adapters.outbound.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pluralkraft.notification.domain.model.Notification;
import com.pluralkraft.notification.domain.model.Receipt;
import com.pluralkraft.notification.ports.out.NotificationSender;

public class WebNotificationSender implements NotificationSender {

    private static final Logger LOG = LoggerFactory.getLogger("NoOpNotificationSender");

    @Override
    public Receipt send(Notification notification) {
        LOG.info("Notification {}", notification);
        return new Receipt(true, "SUCCESS");
    }

}
