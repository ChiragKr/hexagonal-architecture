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
