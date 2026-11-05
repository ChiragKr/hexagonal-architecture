## Testing

### Swappable adapters: runtime vs. tests

| Concern | Runtime (bootstrap/src/main) | Integration tests (bootstrap/src/test) |
|---|---|---|
| Persistence | MongoDeliveryRepository | InMemoryDeliveryRepository (synchronized map with the same claim-token / lease semantics, injectable Clock) |
| Notification sender | WebNotificationSender | @MockitoBean NotificationSender; a lambda stub in RetryNotificationUseCaseTest |
| Dead letter queue | KafkaDeadLetterAdapter | Capturing DeadLetterPort inside RetryNotificationUseCaseTest (can simulate a failed publish) |
| Retry trigger | RetryNotificationScheduler | Not registered; RetryNotificationUseCaseTest calls retryFailedDeliveries() directly (plain JUnit, no Spring context) |
| Kafka broker | Apache Kafka 4.0.1 | @EmbeddedKafka (topic notifications) |
| Wiring |	AdapterConfig / UseCaseConfig | Test classes with the same fully qualified names shadow the main ones on the test classpath |

> [!NOTE]
> There are no Spring profiles in this app; the swap happens through the test classpath, not through dev/prod profiles. The topic name notifications is a string literal in both KafkaNotificationConsumer and KafkaQueueAdapter; the DLQ topic and all retry settings come from notification.retry.* in application.yaml (interval, initial-delay, batch-size, max-retries, lease, dlq-topic, dlq-send-timeout).

### Manual Test

#### Kafka Inbound

Open interactive terminal to the kafka container:
```bash
docker exec -it kafka sh
```

Create a kafka-console-producer.sh and send messages:
```
/ $ /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic notifications
>{"id": "9fa17474-a9dc-4ec0-9751-368d48dc179d","payload": "SOME-KAFKA-PAYLOAD"}
>{"id": "e87dae8f-ebbd-4c2a-b96f-8462ee9e6951","payload": "MORE-KAFKA-PAYLOAD"}
```

Verify application logs:
```
% docker logs notification-service | tail -n 2
2026-04-21T17:40:18.956Z  INFO 1 --- [notification] [ntainer#0-0-C-1] NoOpSendNotification                     : Notification Notification[id=9fa17474-a9dc-4ec0-9751-368d48dc179d, payload=SOME-KAFKA-PAYLOAD]
2026-04-21T17:40:32.818Z  INFO 1 --- [notification] [ntainer#0-0-C-1] NoOpSendNotification                     : Notification Notification[id=e87dae8f-ebbd-4c2a-b96f-8462ee9e6951, payload=MORE-KAFKA-PAYLOAD]
```

#### REST Inbound

Use cURL to send HTTP request:
```
curl --verbose --location 'http://localhost:8080/api/notifications/send' \
--header 'Content-Type: application/json' \
--data '{
    "id": "861aa5e0-cc82-4fe1-908e-4f3ef69bcb90",
    "payload": "SOME-PAYLOAD"
}'
```

HTTP response:
```
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /api/notifications/send HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 83
> 
* upload completely sent off: 83 bytes
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Thu, 23 Apr 2026 13:51:53 GMT
< 
* Connection #0 to host localhost left intact
{"success":true,"response":"SUCCESS"}
```

Verify application logs:
```
% docker logs notification-service | tail -n 1
2026-04-23T13:51:53.142Z  INFO 1 --- [notification] [nio-8080-exec-3] NoOpSendNotification                     : Notification Notification[id=861aa5e0-cc82-4fe1-908e-4f3ef69bcb90, payload=SOME-PAYLOAD]
```

#### Kafka Outbound

Open interactive terminal to the kafka container:
```bash
docker exec -it kafka sh
```

Create a kafka-console-consumer.sh and receiver messages:
```
/ $ /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic notifications
```

Use cURL to send HTTP request:
```
curl --verbose --location 'http://localhost:8080/api/notifications/queue' \
--header 'Content-Type: application/json' \
--data '{
    "id": "05cb39db-f296-4e88-ae07-3bbfb9dd739b",
    "payload": "SOME-QUEUE-PAYLOAD"
}'
```

HTTP response:
```
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /api/notifications/queue HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 89
> 
* upload completely sent off: 89 bytes
< HTTP/1.1 202 
< Content-Length: 0
< Date: Thu, 23 Apr 2026 18:55:27 GMT
< 
* Connection #0 to host localhost left intact
```

Verify consumer logs:
```
/ $ /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic notifications
{"id":"05cb39db-f296-4e88-ae07-3bbfb9dd739b","payload":"SOME-QUEUE-PAYLOAD"}
```

Verify application logs:
```
% docker logs notification-service | tail -n 1
2026-04-23T18:55:27.382Z  INFO 1 --- [notification] [ntainer#0-0-C-1] NoOpSendNotification                     : Notification Notification[id=05cb39db-f296-4e88-ae07-3bbfb9dd739b, payload=SOME-QUEUE-PAYLOAD]
```

#### Mongo Outbound

Open interactive terminal to the mongo container:
```bash
docker exec -it mongo sh
```

Use MongoDB shell to connect to the MongoDB:
```
# mongosh --host localhost --username admin --password password
test> use notification
switched to db notification
notification> 
```

Use cURL to send HTTP request:
```
curl --verbose --location 'http://localhost:8080/api/notifications/send' \
--header 'Content-Type: application/json' \
--data '{
    "id": "1448a6d3-994c-48ec-93fb-fa86b125fab4",
    "payload": "SOME-PAYLOAD"
}'
```

HTTP response:
```
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /api/notifications/send HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 83
> 
* upload completely sent off: 83 bytes
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Sat, 25 Apr 2026 11:09:09 GMT
< 
* Connection #0 to host localhost left intact
{"success":true,"response":"SUCCESS"}
```

Verify database record:
```
notification> db.deliveries.find()
[
  {
    _id: 'e0e5df13-5f30-47bc-816f-1f6619cffe25',
    notificationId: '1448a6d3-994c-48ec-93fb-fa86b125fab4',
    status: 'SUCCESS',
    _class: 'com.pluralkraft.notification.adapters.outbound.mongo.MongoDeliveryEntity'
  }
]
```

Verify application logs:
```
% docker logs notification-service | tail -n 1
2026-04-25T11:09:09.099Z  INFO 1 --- [notification] [nio-8080-exec-1] NoOpNotificationSender                   : Notification Notification[id=1448a6d3-994c-48ec-93fb-fa86b125fab4, payload=SOME-PAYLOAD]
```

#### Web Outbound

Use cURL to send HTTP request:

NOTE: For asynchronous processing send request to `http://localhost:8080/api/notifications/queue`
```
curl --verbose --location 'http://localhost:8080/api/notifications/send' \
--header 'Content-Type: application/json' \
--data '{
    "id": "b2486ffb-b04d-4ec2-a85e-e98744ecfdbd",
    "payload": "SOME-PAYLOAD"
}'
```


HTTP response:

NOTE: `PingController` returns HTTP status 500 for 25% requests (randomly). Response will be different in that case.
```
* Host localhost:8080 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> POST /api/notifications/send HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 83
> 
* upload completely sent off: 83 bytes
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Tue, 05 May 2026 18:33:45 GMT
< 
* Connection #0 to host localhost left intact
{"success":true,"response":"SUCCESS"}%
```

Verify application logs:

NOTE: `PingController` returns HTTP status 500 for 25% requests (randomly). Logs will be different in that case.
```
% docker logs notification-service | tail -n 3
2026-05-05T18:33:45.752Z  INFO 1 --- [notification] [nio-8080-exec-7] c.p.n.a.o.web.WebNotificationSender      : Sending notification to web endpoint: http://localhost:8080/ping
2026-05-05T18:33:45.753Z  INFO 1 --- [notification] [nio-8080-exec-8] c.p.n.a.inbound.rest.PingController      : Received ping request with Notificaiton=Notification[id=b2486ffb-b04d-4ec2-a85e-e98744ecfdbd, payload=SOME-PAYLOAD]
2026-05-05T18:33:45.755Z  INFO 1 --- [notification] [nio-8080-exec-7] c.p.n.a.o.web.WebNotificationSender      : Notification sent successfully to: http://localhost:8080/ping
```
