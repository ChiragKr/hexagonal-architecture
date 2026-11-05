
## Components

| Role| Component | Module / package |
|---|---|---|
| Driving adapter | NotificationController | adapters/inbound/rest · adapters.inbound.rest |
| Driving adapter | KafkaNotificationConsumer | adapters/inbound/kafka · adapters.inbound.kafka |
| Driving adapter | RetryNotificationScheduler | adapters/inbound/scheduler · adapters.inbound.scheduler |
| Inbound port | SendNotificationUseCase, QueueNotificationUseCase, RetryNotificationUseCase | ports · ports.in |
| Core | SendNotificationService, QueueNotificationService, RetryNotificationService | application · application |
| Domain model | Notification, Delivery, DeliveryStatus, Receipt, RetrySummary | domain · domain.model |
| Outbound port | NotificationSender, DeliveryRepository, QueueNotificationPort, DeadLetterPort | ports · ports.out |
| Driven adapter | WebNotificationSender | adapters/outbound/web |
| Driven adapter | MongoDeliveryRepository + MongoDeliveryEntity | adapters/outbound/mongo |
| Driven adapter | KafkaQueueAdapter | adapters/outbound/kafka |
| Driven adapter | KafkaDeadLetterAdapter | adapters/outbound/kafka |
| Composition root | NotificationApplication, UseCaseConfig, AdapterConfig | bootstrap · com.pluralkraft.notification, .configuration |