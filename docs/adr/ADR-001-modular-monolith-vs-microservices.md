# ADR-001: Modular Monolith vs. Microservices Architecture

## Status
**Accepted**

## Context
The SATTAEES application connects daily-wage workers (plumbers, electricians, carpenters, painters, laborers) with local customers seeking services. The original codebase suffered from tight coupling, lack of clear package boundaries, entity leakage directly to HTTP endpoints, and missing transaction separation.

When modernizing the architecture, two primary directions were evaluated:
1. Decomposing into independent distributed microservices (e.g., Auth Service, Customer Service, Worker Service, Job Service, Notification Service).
2. Refactoring into a disciplined **Modular Monolith** within a single deployable artifact.

## Decision
We chose a **Modular Monolith** architecture with strict vertical module boundaries (`auth`, `customer`, `worker`, `job`, `review`, `notification`, `common`, `infrastructure`).

Each business module is self-contained with its own:
- REST Controller (HTTP translation only)
- Business Service (transaction management, domain logic)
- JPA Repository (persistence)
- Entities and DTOs (strong API boundary contract)
- Custom Exceptions and Mappers

Cross-module communication occurs via:
1. In-process Java service calls and DTO contracts for synchronous requests.
2. Apache Kafka event streams (`JobEvent`, `ReviewEvent`) for asynchronous side effects (e.g., notifications).

## Consequences

### Positive:
- **Low Operational Complexity:** Single JVM process to deploy, monitor, and scale during early and mid-growth stages.
- **ACID Transactions:** Inter-entity consistency (e.g. updating worker rating when review is committed) is guaranteed within a single relational transaction without requiring two-phase commit (2PC) or complex Saga orchestrators.
- **Fast Local Development:** Developers run one container stack (`docker-compose up`) rather than managing 6+ separate microservices and network meshes.
- **Easy Migration Path:** If a specific module (e.g., `job` or `notification`) experiences 100x traffic in the future, its clear package boundaries and event interfaces allow it to be carved out into a standalone microservice with minimal refactoring.

### Negative:
- All modules share the same database schema (though logically segmented by table ownership).
- Deploying a change requires redeploying the monolith artifact.

## Alternatives Considered
- **Full Microservices with API Gateway:** Rejected as premature optimization. Microservices introduce network latency, distributed tracing overhead, eventual consistency complexities, and deployment orchestration costs without current business necessity.
- **Flat Monolith (Original Architecture):** Rejected due to lack of boundary enforcement, spaghetti dependencies, and lack of maintainability.
