# Backend Engineering Guide: Observability, Metrics & Distributed Tracing

## 1. What Problem It Solves
When a backend application handles hundreds of concurrent requests, log outputs become interleaved in the console. If a customer reports: *"My booking failed at 3:15 PM,"* finding the relevant log lines among 50,000 requests is nearly impossible without a unifying correlation mechanism.

Furthermore, operations teams need real-time insight into:
- Is the database connection pool exhausted?
- What is the JVM heap memory consumption?
- Are HTTP 500 error rates spiking?
- Is the container healthy enough to receive traffic from the load balancer?

**Observability** combines **Logs**, **Metrics**, and **Traces** to provide total visibility into running systems.

---

## 2. Why Sattaees Needs It
In Sattaees:
- Every HTTP request receives a unique `traceId` injected into **SLF4J MDC (Mapped Diagnostic Context)**.
- If a database query fails or a 404/409 occurs, the `traceId` is included in the JSON error response:
  ```json
  {
    "timestamp": "2026-09-26T18:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Worker not found with ID: 99",
    "path": "/api/workers/99",
    "traceId": "c59e78a01123491f"
  }
  ```
- Support engineers can instantly search this `traceId` across all log aggregators.
- **Spring Boot Actuator** and **Prometheus** endpoints monitor JVM garbage collection, HikariCP connection pool usage, and container health.

---

## 3. How It Works

### The MDC Lifecycle in Sattaees:
```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Filter as TraceIdFilter (OncePerRequestFilter)
    participant MDC as SLF4J MDC (ThreadLocal)
    participant Service as Business Service
    participant Handler as GlobalExceptionHandler

    Client->>Filter: Inbound Request (optional X-Trace-Id)
    Filter->>MDC: MDC.put("traceId", traceId)
    Filter->>Service: doFilter(request, response)
    Service->>Service: log.info("Processing...") [Outputs traceId]
    alt Error Occurs
        Service-->>Handler: Throws ResourceNotFoundException
        Handler->>MDC: MDC.get("traceId")
        Handler-->>Client: 404 JSON response with traceId
    else Success
        Service-->>Filter: Response 200 OK
        Filter-->>Client: Response Header X-Trace-Id: <traceId>
    end
    Note over Filter, MDC: finally { MDC.clear(); }
```

---

## 4. How It Integrates with Spring Boot
1. **MDC Filter (`TraceIdFilter.java`):**
   ```java
   @Component
   @Order(Ordered.HIGHEST_PRECEDENCE)
   public class TraceIdFilter extends OncePerRequestFilter {
       @Override
       protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
               throws ServletException, IOException {
           String traceId = req.getHeader("X-Trace-Id");
           if (traceId == null || traceId.isBlank()) {
               traceId = UUID.randomUUID().toString().replace("-", "");
           }
           MDC.put("traceId", traceId);
           res.setHeader("X-Trace-Id", traceId);
           try {
               chain.doFilter(req, res);
           } finally {
               MDC.clear(); // CRITICAL: Avoid thread pool pollution
           }
       }
   }
   ```
2. **Logging Pattern in `application.properties`:**
   ```properties
   logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [traceId=%X{traceId:-NONE}] %-5level %logger{36} - %msg%n
   ```
3. **Actuator & Prometheus:**
   - Dependency: `spring-boot-starter-actuator` and `micrometer-registry-prometheus`.
   - Endpoint `/actuator/health`: Provides liveness and readiness probes for Docker/Kubernetes.
   - Endpoint `/actuator/prometheus`: Exposes metrics for Prometheus scraping.

---

## 5. What Happens Internally
- **MDC and ThreadLocals:** SLF4J's MDC is backed by a `ThreadLocal<Map<String, String>>`. Because Tomcat reuses threads from a thread pool (`http-nio-8080-exec-*`), failing to invoke `MDC.clear()` in a `finally` block means subsequent unrelated requests handled by that thread will inherit the previous user's `traceId`!
- **Micrometer Instrumentation:** Micrometer wraps Tomcat's request handler to record `http_server_requests_seconds` metrics (timers, percentiles, error status tags) using low-overhead concurrent counters.

---

## 6. Alternatives
| Tool | Scope | Pros | Cons |
| :--- | :--- | :--- | :--- |
| **MDC + Custom TraceId** | In-Process / Monolith | Zero external dependencies, ultra-lightweight, high clarity | Does not automatically propagate across network boundaries |
| **OpenTelemetry (OTel)** | Distributed Tracing | W3C standard trace propagation, works with Jaeger/Zipkin | Additional agent overhead, higher configuration complexity |
| **Spring Cloud Sleuth / Micrometer Tracing** | Spring Ecosystem | Automated Brave/OTel integration | Can be heavy for single monolith deployments |

---

## 7. Trade-offs
### Advantages:
- **Instant Triage:** Customer support and QA can directly provide developers with a `traceId` to immediately isolate the root cause.
- **Production Health Guardrails:** Cloud orchestrators (Kubernetes/ECS) automatically restart failed pods based on `/actuator/health`.

### Limitations:
- Excessive debug logging degrades I/O throughput. Log levels must be kept at `INFO` or `WARN` in production.

---

## 8. Common Mistakes
1. **Forgetting `MDC.clear()` in a `finally` Block:** In pooled thread environments, this causes memory leaks and cross-request trace corruption.
2. **Logging Sensitive Data (PII / Secrets):** Never log raw passwords, credit cards, or JWT strings. Configure logging masks where appropriate.
3. **Exposing Unauthenticated Actuator Endpoints:** Exposing `/actuator/env`, `/actuator/heapdump`, or `/actuator/beans` publicly leaks environment variables and secrets. Keep sensitive actuator endpoints secured behind role checks.

---

## 9. Interview Questions & Answers

### Q1: What are the Three Pillars of Observability?
**Answer:**
1. **Logs:** Immutable, timestamped text records of discrete events (e.g. SLF4J Logback).
2. **Metrics:** Numeric aggregated measurements over time intervals (e.g. CPU load, requests per second, HikariCP active connections via Prometheus).
3. **Traces:** Representation of a request's end-to-end journey across functions, threads, and network boundaries (e.g. MDC `traceId`, OpenTelemetry).

### Q2: How does SLF4J MDC work internally, and why is `MDC.clear()` mandatory?
**Answer:** MDC is an abstraction over a `ThreadLocal` map. When `MDC.put(key, val)` is called, the value is bound to the executing thread. Because web servers like Tomcat use thread pools, worker threads are never destroyed; they return to the pool to service future requests. If `MDC.clear()` is omitted, the next HTTP request processed by that thread will log with the previous request's context, causing trace corruption and memory leaks.

### Q3: What is the difference between `/actuator/health/liveness` and `/actuator/health/readiness`?
**Answer:**
- **Liveness:** Tells the orchestrator (Kubernetes/Docker) if the JVM process is alive. If liveness fails, the container should be killed and restarted.
- **Readiness:** Tells the load balancer if the application is ready to accept incoming traffic (e.g. database connections are established and caches are warmed up). If readiness fails, traffic is diverted away without killing the container.
