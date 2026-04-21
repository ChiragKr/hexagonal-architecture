# Bootstrap Module - Spring Boot Application

This module contains the main Spring Boot application that serves as the entry point for the notification service.

## Containerization

This module is containerized using Docker to enable easy deployment and execution in different environments.

### Building the Docker Image

To build the Docker image for this module:

```bash
# Build the Docker image
docker build -t notification-service:latest .
```

### Running with Docker Compose

This project includes a `docker-compose.yml` file that defines how to run the application with its dependencies (Kafka).

To run the complete stack:

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down
```

### Running the Application Directly

You can also run the application directly using Maven:

```bash
# Build and run with Maven
mvn spring-boot:run
```

### Environment Variables

The application supports the following environment variables:

- `SPRING_PROFILES_ACTIVE`: Set to "docker" for containerized environments
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka server address (defaults to localhost:9092)

## Dockerfile Structure

The Dockerfile uses a multi-stage build approach:
1. **Builder Stage**: Uses Maven to compile and package the application
2. **Runtime Stage**: Uses a minimal OpenJDK JRE to run the application

This approach ensures that the final Docker image is as small as possible while maintaining full functionality.