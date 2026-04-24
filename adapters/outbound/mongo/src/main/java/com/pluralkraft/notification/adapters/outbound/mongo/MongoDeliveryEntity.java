package com.pluralkraft.notification.adapters.outbound.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.pluralkraft.notification.domain.model.DeliveryStatus;

@Document(collection = "deliveries")
public class MongoDeliveryEntity {

    @Id
    private String id;

    private String notificationId;

    private DeliveryStatus status;

    public MongoDeliveryEntity() {
    }

    public MongoDeliveryEntity(String id, String notificationId, DeliveryStatus status) {
        this.id = id;
        this.notificationId = notificationId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
}