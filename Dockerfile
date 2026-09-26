# ====================================================================
# Multi-stage Dockerfile for Sattaees Modular Monolith Backend
# ====================================================================

# Stage 1: Build & Package
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Cache maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source tree and package executable jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal, Hardened Production Runtime
FROM eclipse-temurin:17-jre-alpine

# Security: Create non-root system group and user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder --chown=spring:spring /workspace/target/sattaees-*.jar app.jar

# Expose HTTP service port
EXPOSE 8080

# Production environment defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Healthcheck monitoring Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
