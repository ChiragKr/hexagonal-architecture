# ==========================================
# Stage 1: Build the application
# ==========================================
# Use the official Maven 3.9.9 with OpenJDK 21 image as the base image
FROM maven:3-eclipse-temurin-21-alpine AS builder

# Set working directory inside the container
WORKDIR /build

# Copy the entire project source code
COPY . .

# Build only the Spring Boot module and its required dependencies (-am).
# We use a BuildKit cache mount to avoid re-downloading Maven dependencies on every build.
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl bootstrap -am -DskipTests && \
    cp /build/bootstrap/target/*.jar app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# ==========================================
# Stage 2: Create runtime image
# ==========================================
# Use a slim JRE image for the final runtime
FROM eclipse-temurin:21-jre-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the extracted layers in order of how frequently they change.
# Dependencies change rarely, so Docker will cache these layers heavily.
COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
# Application code changes often, so this layer is rebuilt, but the ones above are cached.
COPY --from=builder /build/extracted/application/ ./

# Expose port 8080 (default Spring Boot port)
EXPOSE 8080

# Create a non-root user for security
RUN addgroup --system spring && \
    adduser --system spring && \
    chown -R spring:spring /app
USER spring:spring

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

# Launch the application using the JarLauncher, injecting JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]