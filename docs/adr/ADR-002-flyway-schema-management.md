# ADR-002: Flyway Database Schema Migration Strategy

## Status
**Accepted**

## Context
The legacy application relied on `spring.jpa.hibernate.ddl-auto=update` to generate and modify database tables at runtime. In production environments, relying on Hibernate for automatic DDL evolution introduces severe risks:
1. **Uncontrolled Schema Drift:** Schema changes made on a developer's machine or staging environment are not systematically tracked or reproducible.
2. **Data Loss & Downtime:** Hibernate may drop or alter columns unexpectedly or lock production tables during startup.
3. **No Rollback Capability:** There is no audit history of what changes were applied, when, or by whom.
4. **Validation Mismatch:** Differences in data types between Hibernate's auto-generated DDL and production requirements can cause subtle runtime failures.

## Decision
We adopted **Flyway** for version-controlled, automated database migrations, and set Hibernate's DDL behavior to `validate` in production (`spring.jpa.hibernate.ddl-auto=validate`).

Migration scripts reside in `src/main/resources/db/migration/`:
- `V1__init_schema.sql`: Full baseline schema with explicit data types, primary keys, foreign keys, unique constraints, optimistic locking `version` columns, and performance indexes.
- `V2__seed_initial_data.sql`: Deterministic initial seed data with pre-hashed BCrypt passwords for demo customer and worker accounts.

## Consequences

### Positive:
- **Deterministic Deployments:** Every environment (local development, CI test pipeline, staging, production) executes the exact same migration scripts in identical order.
- **Auditability:** Flyway maintains the `flyway_schema_history` table detailing the version, description, checksum, execution time, and success status of every script.
- **Zero Ambiguity:** Developers inspect plain SQL scripts to understand indexes, foreign key cascades, and column nullability rather than guessing Hibernate's dialect defaults.

### Negative:
- Developers must write explicit SQL migration scripts for every schema change rather than letting Hibernate auto-alter tables.

## Alternatives Considered
- **Liquibase:** Highly capable with XML/YAML/SQL formats, but XML/YAML DSL introduces unnecessary verbosity and learning curve. Flyway's native SQL approach was preferred for developer ergonomics and clarity.
- **Hibernate `ddl-auto=update`:** Strictly rejected for production use due to lack of predictability and risk of irreversible data corruption.
