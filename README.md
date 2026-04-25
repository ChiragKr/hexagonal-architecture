# hexagonal-architecture
This project demonstrates the core principles of Hexagonal Architecture (Ports and Adapters). The architecture ensures that business logic remains decoupled from frameworks, databases, and external APIs. By decoupling the business logic from external infrastructure, the system remains maintainable, testable, and adaptable to changing technologies.

## Building and Running

### Prerequisites
- Java 21
- Maven
- Docker and Docker Compose

### Building the Application
To build the application:
```bash
mvn clean package
```

This will create a Spring Boot executable JAR in the `bootstrap/target` directory.

### Running with Docker Compose
To run the application using Docker Compose:
```bash
docker-compose up
```

This will:
1. Build the notification-service container from the bootstrap module
2. Start the Kafka broker on port 9092
3. Start the notification-service on port 8080

### Running Locally (without containers)
To run the application locally:
```bash
cd bootstrap
mvn spring-boot:run
```

The application will be available at http://localhost:8080

## Testing

### Kafka Inbound

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

### REST Inbound

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

### Kafka Outbound

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

### Mongo Outbound

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