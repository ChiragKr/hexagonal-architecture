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

Verify logs notification-service logs:
```
% docker logs notification-service | tail -n 2
2026-04-21T17:40:18.956Z  INFO 1 --- [notification] [ntainer#0-0-C-1] NoOpSendNotification                     : Notification Notification[id=9fa17474-a9dc-4ec0-9751-368d48dc179d, payload=SOME-KAFKA-PAYLOAD]
2026-04-21T17:40:32.818Z  INFO 1 --- [notification] [ntainer#0-0-C-1] NoOpSendNotification                     : Notification Notification[id=e87dae8f-ebbd-4c2a-b96f-8462ee9e6951, payload=MORE-KAFKA-PAYLOAD]
```
