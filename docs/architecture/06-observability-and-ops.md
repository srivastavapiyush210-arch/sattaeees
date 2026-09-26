# Sattaees Architecture: Observability, Logging & Operations

## 1. Observability Pillars

Production systems require rapid diagnostic capabilities to track requests across threads, database queries, and external broker calls. Sattaees implements three core pillars:

```mermaid
graph TD
    A[Incoming HTTP Request] --> B[TraceIdFilter]
    B --> C[Inject traceId into SLF4J MDC]
    B --> D[Add X-Trace-Id Header to HTTP Response]
    C --> E[Application Logs with traceId]
    C --> F[GlobalExceptionHandler Error Payload with traceId]
    G[Spring Boot Actuator] --> H[/actuator/health]
    G --> I[/actuator/prometheus]
    I --> J[Prometheus Server]
    J --> K[Grafana Dashboards]
```

---

## 2. Distributed Tracing & Request Correlation (MDC)

### Implementation
Every inbound HTTP request passes through `TraceIdFilter`:
1. Inspects incoming headers for `X-Trace-Id` or `X-Correlation-Id`. If missing, generates a new 32-character UUID.
2. Injects the ID into SLF4J's **Mapped Diagnostic Context (MDC)** under the key `traceId`.
3. Appends `X-Trace-Id` to the HTTP response header for client-side correlation.
4. Clears MDC in a `finally` block to prevent thread pool contamination in Tomcat worker threads.

### Logging Configuration:
```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [traceId=%X{traceId:-NONE}] %-5level %logger{36} - %msg%n
```

### Log Output Example:
```text
2026-09-26 18:30:15.120 [http-nio-8080-exec-1] [traceId=a81f3d90b4114251a37c9f69b18361ab] INFO  c.s.s.job.service.JobRequestService - Creating job request for customerId: 1, workerId: 2, service: PLUMBING
2026-09-26 18:30:15.155 [http-nio-8080-exec-1] [traceId=a81f3d90b4114251a37c9f69b18361ab] INFO  c.s.s.j.s.JobRequestService - Job request created with ID: 105. Status: PENDING
```

### Trace ID in API Error Responses:
When an unhandled exception or business validation error occurs, the client receives the `traceId` in the JSON response:
```json
{
  "timestamp": "2026-09-26T18:30:15.200",
  "status": 404,
  "error": "Not Found",
  "message": "Worker not found with ID: 99",
  "path": "/api/workers/99",
  "traceId": "a81f3d90b4114251a37c9f69b18361ab"
}
```
A support engineer can copy `a81f3d90b4114251a37c9f69b18361ab` directly into Logstash/Datadog/CloudWatch to view the complete execution sequence.

---

## 3. Metrics & Health Checks (Actuator + Prometheus)

### Actuator Configuration
- **Health endpoint:** `/actuator/health` exposes database status, disk space, and application liveness. Configured with `show-details: when-authorized` to prevent leaking internal database hostnames to unauthenticated callers.
- **Prometheus scraper:** `/actuator/prometheus` exposes OpenMetrics format counters, gauges, and timers for JVM memory, CPU utilization, HikariCP pool saturation, and HTTP request durations (`http_server_requests_seconds`).

### Prometheus Scraping Configuration:
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'sattaees-backend'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['backend:8080']
```

---

## 4. Containerization & Production Operations

### Multi-Stage Docker Build
Sattaees uses a two-stage Docker build to minimize image size and eliminate build tool security vulnerabilities in runtime environments:

1. **Build Stage (`eclipse-temurin:17-jdk-alpine`):**
   - Copies Maven wrapper and `pom.xml` first to leverage Docker layer caching for dependencies.
   - Compiles and packages application jar using `mvn package -DskipTests`.
2. **Runtime Stage (`eclipse-temurin:17-jre-alpine`):**
   - Contains only the lightweight JRE (image size reduced from ~600MB to ~180MB).
   - Creates a dedicated non-privileged user and group: `spring:spring` (UID 10001).
   - Runs `ENTRYPOINT ["java", "-jar", "app.jar"]` as non-root.

### Production Container Hardening in `docker-compose.yml`:
- **Healthchecks:** Configured via `wget --spider http://localhost:8080/actuator/health`.
- **Resource Constraints:** `cpus: '1.0'`, `memory: 512M` to protect host kernels from JVM OutOfMemory thrashing.
- **Restart Policies:** `restart: unless-stopped` ensures automated self-healing.
- **Isolated Networks:** All containers communicate via internal Docker bridge network `sattaees-network`.
