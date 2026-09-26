# Backend Engineering Guide: Modular Monolith Architecture

## 1. What Problem It Solves
In software engineering, teams often struggle between two extremes:
- **The Spaghetti Monolith:** Everything is tossed into a single codebase with no package encapsulation, leaky abstractions, circular dependencies, direct database entity sharing, and untracked side-effects. Over time, making a small change in one area breaks unrelated features.
- **The Distributed Microservices Nightmare:** Splitting an early-stage application into dozens of small services introduces distributed transactions, network latency, serialization overhead, API versioning chaos, distributed tracing complexity, and high cloud infrastructure bills—before the product has even found product-market fit.

A **Modular Monolith** solves this dilemma. It runs as a **single deployable artifact** (a single JVM process, single container) while strictly enforcing **domain-driven vertical boundaries** inside the codebase.

---

## 2. Why Sattaees Needs It
Sattaees connects daily wage laborers (plumbers, electricians, painters) with local customers. The application has clear business domains:
- Identity & Access (`auth`)
- Customer Management (`customer`)
- Worker Profiles & Ratings (`worker`)
- Booking State Machine (`job`)
- Customer Feedback (`review`)
- Event Notifications (`notification`)

If built as microservices, booking a job would require a distributed Saga across Auth, Worker, Customer, and Job services, with two-phase commits for transactions. If built as a spaghetti monolith, controllers would manipulate JPA entities directly, risking accidental data corruption. The modular monolith gives Sattaees ACID relational guarantees with clean, decoupled domain packages.

---

## 3. How It Works
The architecture organizes code by **feature/domain** rather than by technical layer:

```text
com.sattaees.sattaees/
├── common/             # Cross-cutting: Base entities, error responses, pagination
├── infrastructure/     # Technical drivers: Security, Redis, Kafka, OpenAPI, Auditing
├── auth/               # Identity: Login, registration, token refresh, RBAC
├── customer/           # Customer domain: Profiles, customer lookups
├── worker/             # Worker domain: Skills, hourly rates, availability, caching
├── job/                # Job domain: Booking state machine, concurrency, Kafka events
├── review/             # Review domain: Ratings, review aggregation, cache eviction
└── notification/       # Notification domain: Kafka consumer, idempotency, alerts
```

### Module Boundary Rules:
1. **Controllers Handle HTTP Only:** Controllers deserialize JSON, trigger Bean Validation, call services, and return DTOs with HTTP status codes. No business rules or persistence calls belong in controllers.
2. **Services Contain Domain Rules:** All state transitions, authorization ownership checks, and database mutations are encapsulated in `@Service` classes annotated with `@Transactional`.
3. **No Direct Entity Exposure:** JPA entities never escape the service layer into REST responses. Mappers convert entities to response DTOs.
4. **Cross-Module Communication:**
   - Synchronous queries use typed service interfaces and DTOs.
   - Asynchronous decoupled reactions use Kafka domain events (`JobEvent`, `ReviewEvent`).

---

## 4. How It Integrates with Spring Boot
Spring Boot provides natural support for modular design:
- **Component Scanning:** Scans base packages and registers beans automatically.
- **Package-Private Encapsulation:** Repositories and internal helpers can be package-private to prevent unauthorized access from other packages.
- **Profiles:** Profiles (`local`, `prod`, `test`) allow modular infrastructure configuration (e.g., in-memory H2 vs PostgreSQL).
- **Spring Application Events / Kafka:** Spring's event publication mechanism allows synchronous or asynchronous domain event dissemination.

---

## 5. What Happens Internally
At runtime in the JVM:
1. The Spring IoC container initializes all singletons based on dependency graph resolution.
2. Cross-module calls (e.g. `JobRequestService` calling `CustomerRepository` or emitting a Kafka event) are resolved as direct in-memory method invocations on dynamic proxies (`@Transactional` proxies).
3. No network hops, serialization, or RPC overhead occur during module-to-module synchronous communication.
4. Database transactions span the unified HikariCP connection pool, enabling rollback of entire multi-step domain operations if an error occurs.

---

## 6. Alternatives
| Architecture | Description | Pros | Cons |
| :--- | :--- | :--- | :--- |
| **Layered Monolith** | Packages organized as `controller`, `service`, `dao`, `model` | Easy to grasp initially | Loses domain boundaries; promotes spaghetti dependencies |
| **Modular Monolith** | Vertical packages by domain (`auth`, `job`, `worker`) | Strong boundaries, high performance, ACID transactions, easy local dev | Entire app redeploys together |
| **Microservices** | Independent repositories, containers, and databases per service | Independent deployments, polyglot tech stacks | Distributed transactions, network latency, ops overhead |

---

## 7. Trade-offs
### Advantages:
- **Maximum Developer Velocity:** Run and debug the entire application locally in IntelliJ/VS Code with a single click.
- **ACID Transactions:** Inter-domain consistency (e.g., updating worker rating upon review creation) occurs in a single database transaction.
- **Extensible Evolution:** Modules can be extracted into standalone microservices in the future with zero business logic rewriting because contracts are already established.

### Limitations:
- **Shared Database:** Modules share the same database instance. Care must be taken not to join tables indiscriminately across module boundaries.
- **Single Deployment Unit:** If one module has a memory leak or crash, the entire JVM process goes down.

---

## 8. Common Mistakes
1. **Leaking JPA Entities Across Boundaries:** Returning `@Entity` classes directly from controllers causes infinite JSON recursion, lazy initialization exceptions, and accidental field exposure (e.g., password hashes). Always use DTOs.
2. **Circular Module Dependencies:** Module A depends on Module B, which depends on Module A. Break circular dependencies by introducing domain events or extracting shared abstractions into `common`.
3. **Database Joins Across Everything:** Writing JPQL queries that join `Customer` -> `JobRequest` -> `Worker` -> `Review` -> `Notification` tightly couples tables and causes performance bottlenecks.

---

## 9. Interview Questions & Answers

### Q1: When should you choose a Modular Monolith over Microservices?
**Answer:** You should choose a Modular Monolith when the domain is evolving, team size is moderate (< 50 engineers), and operational simplicity is desired. Microservices solve organizational scaling problems (e.g., multiple independent teams deploying independently) rather than performance problems. A modular monolith provides clean boundaries, fast iteration, and ACID transactions without the operational overhead of distributed systems.

### Q2: How do you prevent developers from violating module boundaries in a monolithic codebase?
**Answer:** 
1. Use Java package-private visibility so internal implementation classes cannot be imported outside the package.
2. Enforce architectural boundaries via automated unit tests using **ArchUnit**.
3. Use compiler tools like Java 9+ Modules (JPMS) or Gradle/Maven multi-module subprojects where each module has its own `pom.xml`.

### Q3: How do you migrate a module out of a modular monolith into a microservice?
**Answer:** Because the module already has dedicated REST controllers, service interfaces, DTOs, and Kafka event listeners:
1. Extract the module package into a separate Git repository and Spring Boot application.
2. Split the shared database tables into a dedicated database for that service.
3. Replace direct in-memory Java service calls with HTTP REST clients (OpenFeign) or asynchronous Kafka message exchanges.
