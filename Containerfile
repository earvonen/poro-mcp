# Containerfile for OpenShift
# Stage 1: Build
FROM registry.access.redhat.com/ubi9/openjdk-21:latest AS build
USER root
WORKDIR /build

COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest
WORKDIR /app

# Copy the built application
COPY --from=build /build/target/quarkus-app/lib/ /app/lib/
COPY --from=build /build/target/quarkus-app/*.jar /app/
COPY --from=build /build/target/quarkus-app/app/ /app/app/
COPY --from=build /build/target/quarkus-app/quarkus/ /app/quarkus/

# Set up environment
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Run as non-root user (OpenShift requirement)
USER 1001

ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]

