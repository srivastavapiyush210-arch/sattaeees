# Sattaees (सत्ताईस) - Production-Grade Modular Monolith Reference Architecture

[![Java CI with Maven](https://github.com/srivastavapiyush210-arch/sattaeees/actions/workflows/ci.yml/badge.svg)](https://github.com/srivastavapiyush210-arch/sattaeees/actions/workflows/ci.yml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Kafka-KRaft%207.5-black.svg)](https://kafka.apache.org/)

**Sattaees** is a production-grade, highly scalable backend engineering reference project and local labor marketplace platform built in Java 17 and Spring Boot 3. It connects daily-wage skilled laborers (plumbers, electricians, carpenters, painters, masons) with local customers.

The system is designed as a **Modular Monolith** and serves as both a high-fidelity functional application and an **educational backend engineering blueprint** demonstrating real-world patterns: stateless JWT authentication with token rotation, database migrations via Flyway, optimistic locking for concurrency control, Redis cache-aside with fault-tolerant fallbacks, asynchronous event streaming with Apache Kafka and Dead Letter Topics, SLF4J MDC distributed tracing, and Prometheus observability.

---

## 🏗️ System Architecture

```mermaid
flowchart TB
    subgraph CLIENT ["Client Layer"]
        REACT["React Frontend (Vite)"]
        MOBILE["Mobile / Third-Party REST Clients"]
    end

    subgraph INGRESS ["Ingress & Observability"]
        TRACE["TraceIdFilter (MDC Correlation ID)"]
        SEC["Spring Security FilterChain (Stateless JWT)"]
    end

    subgraph MONOLITH ["Sattaees Modular Monolith (Spring Boot 3 / JVM)"]
        direction TB
        subgraph MODULES ["Domain Modules"]
            AUTH["auth: Authentication & Refresh Tokens"]
            CUST["customer: Customer Profiles"]
            WORK["worker: Worker Catalog & Availability"]
            JOB["job: Booking State Machine & Locking"]
            REV["review: Ratings & Feedback Aggregation"]
            NOTIF["notification: Event Consumers & Idempotency"]
        end

        subgraph CORE ["Cross-Cutting Core"]
            COMM["common: ErrorResponse, Auditing, TraceFilter"]
            INFRA["infrastructure: Redis, Kafka, OpenAPI, Security"]
        end
    end

    subgraph DATA_INFRA ["Infrastructure & Persistence Layer"]
        PG[(PostgreSQL 15 - Primary Relational DB)]
        REDIS[(Redis 7 - Distributed Cache)]
        KAFKA[(Apache Kafka - KRaft Event Bus)]
        PROM[Prometheus / Actuator Metrics]
    end

    REACT -->|HTTP / Bearer Token| TRACE
    MOBILE -->|HTTP / Bearer Token| TRACE
    TRACE --> SEC
    SEC --> MODULES
    
    MODULES --> PG
    WORK <-->|Cache-Aside (workers:profile)| REDIS
    JOB -->|sattaees.job.events| KAFKA
    REV -->|sattaees.review.events| KAFKA
    KAFKA -->|NotificationConsumer| NOTIF
    MONOLITH -.->|/actuator/prometheus| PROM
```

---

## 🛠️ Technology Stack & Architectural Justification

| Technology | Role | Justification in Sattaees |
| :--- | :--- | :--- |
| **Java 17 & Spring Boot 3.2.1** | Application Framework | Modern LTS Java runtime, virtual-thread readiness, component-based security, robust ecosystem. |
| **PostgreSQL 15** | Relational Database | ACID transactions for bookings, foreign key integrity, multi-column search indexes, and MVCC concurrency. |
| **Flyway 9.22** | Schema Migrations | Version-controlled, reproducible database migrations (`V1`, `V2`). Eliminates dangerous runtime `ddl-auto=update`. |
| **Hibernate 6 / Spring Data JPA** | ORM & Persistence | Declarative repositories, dirty checking, `@EntityGraph` for N+1 query prevention, `@Version` for optimistic locking. |
| **Redis 7** | Distributed Cache | Cache-aside for worker catalogs and profiles (`workers:profile`, `workers:search`) with custom resilient fallback. |
| **Apache Kafka 7.5 (KRaft)** | Event Streaming | Decouples job bookings and review submissions from downstream notifications; includes Dead Letter Topics (`.DLT`). |
| **Spring Security 6 & JWT** | Identity & Access | Dual-token authentication (Access Token 60m + Refresh Token 7d), BCrypt hashing, method-level RBAC. |
| **Spring Boot Actuator & Prometheus** | Observability | Health checks (`/actuator/health`), JVM metrics, HikariCP connection pool metrics (`/actuator/prometheus`). |
| **SLF4J & Logback (MDC)** | Distributed Tracing | Correlation ID (`traceId`) injected into logs, HTTP response headers, and standardized error responses. |
| **Testcontainers** | Integration Testing | Spin up isolated real PostgreSQL containers during automated integration test runs. |
| **Docker & Docker Compose** | Containerization | Multi-stage Dockerfile (Alpine JRE, non-root user) and local multi-service orchestration. |

---

## 🚀 Quick Start (Local Development)

### Prerequisites
- **JDK 17+** installed and configured (`JAVA_HOME`).
- **Maven 3.8+** (or use included `./mvnw`).
- **Docker & Docker Compose** (for multi-container deployment).
- **Node.js 18+** (for running frontend development server).

### Option 1: Run with Docker Compose (Recommended)
This starts PostgreSQL, Redis, Kafka, Spring Boot backend, and React frontend in isolated containers:

```bash
# Clone the repository
git clone https://github.com/srivastavapiyush210-arch/sattaeees.git
cd sattaeees

# Copy environment variables template
cp .env.example .env

# Build and launch all services
docker-compose up --build
```

- **Frontend:** `http://localhost:5173`
- **Backend API:** `http://localhost:8080`
- **Swagger / OpenAPI Documentation:** `http://localhost:8080/swagger-ui.html`
- **Actuator Health:** `http://localhost:8080/actuator/health`
- **Prometheus Metrics:** `http://localhost:8080/actuator/prometheus`

---

### Option 2: Run Backend Locally (H2 / Dev Profile)
The backend includes a pre-configured `local` profile utilizing an in-memory database with PostgreSQL compatibility:

```bash
# Run unit & integration tests
./mvnw clean test

# Run application locally
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Access the H2 Web Console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:sattaeesdb`, User: `sa`, Password: `password`).

---

## 🔑 Demo Credentials (Pre-Seeded via Flyway `V2`)

| Role | Email | Password | Description |
| :--- | :--- | :--- | :--- |
| **Customer** | `demo@customer.com` | `password123` | Demo customer with pre-configured bookings |
| **Worker** | `ramesh.kumar@sattaees.com` | `password123` | Master Electrician (5 yrs experience, ₹350/hr) |
| **Worker** | `suresh.patel@sattaees.com` | `password123` | Certified Plumber (8 yrs experience, ₹400/hr) |
| **Worker** | `vikram.singh@sattaees.com` | `password123` | Master Carpenter (10 yrs experience, ₹450/hr) |

---

## 📡 REST API Reference

All requests and responses use JSON. Authenticated endpoints require `Authorization: Bearer <accessToken>`.

### Standard Error Response Format
```json
{
  "timestamp": "2026-09-26T18:30:15.120",
  "status": 404,
  "error": "Not Found",
  "message": "Worker not found with ID: 99",
  "path": "/api/workers/99",
  "traceId": "a81f3d90b4114251a37c9f69b18361ab"
}
```

### Core API Endpoints

#### 1. Authentication (`/api/auth`)
- `POST /api/auth/register/customer` - Register a customer account
- `POST /api/auth/register/worker` - Register a worker profile
- `POST /api/auth/login` - Authenticate with email/password; returns JWT access & refresh tokens
- `POST /api/auth/refresh` - Rotate and exchange refresh token for a new access token
- `POST /api/auth/logout` - Revoke refresh token

#### 2. Worker Directory (`/api/workers`)
- `GET /api/workers` - List all workers (Cached in Redis `workers:search`)
- `GET /api/workers/{id}` - Get worker profile by ID (Cached in Redis `workers:profile`)
- `GET /api/workers/search?skill=...&city=...` - Filter workers by trade and city
- `PUT /api/workers/{id}` - Update worker profile (Evicts cache, verifies ownership)
- `PATCH /api/workers/{id}/availability?available=true` - Toggle worker active status

#### 3. Job Booking Lifecycle (`/api/job-requests`)
- `POST /api/job-requests` - Create booking request (Emits `JobEvent` to Kafka)
- `GET /api/job-requests/{id}` - View booking details
- `GET /api/job-requests/my-jobs` - Get bookings for the authenticated user
- `PATCH /api/job-requests/{id}/status?status=ACCEPTED` - Transition job state (Optimistic locking protected)
- `PATCH /api/job-requests/{id}/cancel` - Cancel booking

#### 4. Reviews & Ratings (`/api/reviews`)
- `POST /api/reviews` - Submit review (Updates worker average rating atomically, evicts cache, publishes `ReviewEvent`)
- `GET /api/reviews/worker/{workerId}` - View reviews for a worker

---

## 📚 Backend Engineering Learning Guides

Explore deep-dive technical documentation designed for backend engineers in [`/docs`](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/):

- [Modular Monolith Architecture Guide](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/backend-learning/01-modular-monolith.md)
- [Spring Security & Stateless JWT Masterclass](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/backend-learning/02-spring-security-jwt.md)
- [JPA, Hibernate & Optimistic Locking Guide](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/backend-learning/03-jpa-hibernate-optimistic-locking.md)
- [Redis Caching & Fault Tolerance Blueprint](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/backend-learning/04-redis-caching.md)
- [Apache Kafka Event Streaming & DLT Architecture](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/backend-learning/05-apache-kafka.md)
- [Flyway Database Migration Strategy](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/backend-learning/06-flyway-migrations.md)
- [Observability, Metrics & MDC Distributed Tracing](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/backend-learning/07-observability-tracing.md)
- [Senior Backend Engineering Interview Guide](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/backend-learning/08-backend-interview-guide.md)

### Architecture Decision Records (ADRs)
- [ADR-001: Modular Monolith vs. Microservices](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/adr/ADR-001-modular-monolith-vs-microservices.md)
- [ADR-002: Flyway Schema Management](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/adr/ADR-002-flyway-schema-management.md)
- [ADR-003: Stateless JWT with Refresh Tokens](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/adr/ADR-003-stateless-jwt-with-refresh-tokens.md)
- [ADR-004: Redis Cache-Aside with Fault Tolerance](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/adr/ADR-004-redis-cache-aside-with-fault-tolerance.md)
- [ADR-005: Kafka Event Streaming & DLT](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/adr/ADR-005-kafka-event-streaming-and-dlt.md)
- [ADR-006: Optimistic Locking for Concurrency Control](file:///c:/Users/sriva/OneDrive/Desktop/sattaees/docs/adr/ADR-006-optimistic-locking-for-concurrency.md)

---

## 🧪 Testing Strategy

The project contains a comprehensive test suite covering all layers:

```bash
# Run the complete test suite
./mvnw test
```

### Test Coverage Highlights:
- **Unit Tests:** Business logic testing with Mockito (`WorkerServiceTest`, `JobRequestServiceTest`, `AuthServiceTest`).
- **Controller Slice Tests:** MockMvc testing for request validation, HTTP status codes, and error formatting (`WorkerControllerTest`, `JobRequestControllerTest`).
- **Security Tests:** Validating protected endpoints, role verification, and expired token rejection (`SecurityIntegrationTest`).
- **Concurrency Integration Tests:** Validating optimistic locking behavior under simulated concurrent status transitions (`JobConcurrencyIntegrationTest`).

---

## 📄 License
This project is licensed under the MIT License - see the `LICENSE` file for details.
