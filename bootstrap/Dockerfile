# Use a slim JRE image for the final runtime
FROM eclipse-temurin:21-jre-alpine

# Set working directory inside the container
WORKDIR /app

# Copy application binaries
COPY target/bootstrap-*.jar app.jar

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