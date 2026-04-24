package com.pluralkraft.notification.adapters.outbound.mongo;

import com.pluralkraft.notification.domain.model.Delivery;
import com.pluralkraft.notification.domain.model.DeliveryStatus;
import com.pluralkraft.notification.ports.out.DeliveryRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class MongoDeliveryRepository implements DeliveryRepository {

    private MongoTemplate mongoTemplate;

    public MongoDeliveryRepository(
        MongoTemplate mongoTemplate
    ) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Delivery save(Delivery delivery) {
        MongoDeliveryEntity entity = new MongoDeliveryEntity(
            delivery.id(),
            delivery.notificationId(),
            delivery.status()
        );

        entity = mongoTemplate.save(entity);

        return new Delivery(
            entity.getId(),
            entity.getNotificationId(),
            entity.getStatus()
        );
    }

    @Override
    public void updateStatus(String deliveryId, DeliveryStatus status) {
        Query query = new Query(Criteria.where("id").is(deliveryId));
        Update update = new Update().set("status", status);

        mongoTemplate.updateFirst(query, update, MongoDeliveryEntity.class);
    }
}
