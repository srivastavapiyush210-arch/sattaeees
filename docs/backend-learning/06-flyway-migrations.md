# Backend Engineering Guide: Database Migrations with Flyway

## 1. What Problem It Solves
In beginner projects, developers frequently rely on Hibernate's automatic schema generation:
```properties
spring.jpa.hibernate.ddl-auto=update
```
In enterprise and production backends, this is considered an **anti-pattern**:
- **Schema Drift:** Differences accumulate between local developer databases, staging servers, and production instances.
- **Accidental Data Loss:** Hibernate might silently drop columns, truncate tables, or recreate foreign keys in unexpected ways.
- **No Version History:** There is no audit record of who made a schema change, when it was applied, or why.
- **Lock Contention:** Hibernate altering table definitions dynamically on startup can lock production tables during high-traffic deployments.

**Flyway** solves this by providing version-controlled, reproducible, and auditable database migrations using plain SQL scripts.

---

## 2. Why Sattaees Needs It
Sattaees requires strict database governance:
- **`V1__init_schema.sql`**: Defines all relational tables (`customers`, `workers`, `job_requests`, `reviews`, `refresh_tokens`), indexes (`idx_workers_skill_city`), foreign keys, and optimistic locking `version` columns.
- **`V2__seed_initial_data.sql`**: Inserts predictable seed accounts (e.g. demo customer and 8 certified trade workers with pre-hashed BCrypt passwords) for instant local testing and frontend demoing.
- Production configuration enforces `spring.jpa.hibernate.ddl-auto=validate` so Hibernate validates that entities match the Flyway schema without attempting any DDL modifications.

---

## 3. How It Works: Naming Conventions & Schema History
Flyway discovers SQL files in `src/main/resources/db/migration/` following a strict naming convention:

```text
V<Version>__<Description>.sql
```
- `V`: Prefix for versioned migrations (executed once in numerical order).
- `<Version>`: Numbers separated by dots or underscores (e.g. `V1`, `V2`, `V2.1`).
- `__`: Two underscores separate the version from the description.
- `<Description>`: Human-readable explanation (`init_schema`, `add_user_phone`).

### The `flyway_schema_history` Table
On first startup, Flyway creates this metadata table:
| installed_rank | version | description | type | script | checksum | installed_on | execution_time | success |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | 1 | init schema | SQL | V1__init_schema.sql | -14298124 | 2026-09-26 18:00 | 45ms | true |
| 2 | 2 | seed initial data | SQL | V2__seed_initial_data.sql | 87123984 | 2026-09-26 18:00 | 22ms | true |

---

## 4. How It Integrates with Spring Boot
1. **Dependency:** `flyway-core` added to `pom.xml`.
2. **Execution Timing:** In Spring Boot's startup lifecycle, Flyway migrations run **BEFORE** the JPA `EntityManagerFactory` initializes. This guarantees the database schema exists before Hibernate validates entity mappings.
3. **Configuration:**
   ```properties
   spring.flyway.enabled=true
   spring.flyway.baseline-on-migrate=true
   spring.flyway.locations=classpath:db/migration
   spring.jpa.hibernate.ddl-auto=validate
   ```

---

## 5. What Happens Internally
1. **Database Lock Acquisition:** Flyway acquires an exclusive table lock on the metadata table to prevent race conditions when multiple application pods boot simultaneously.
2. **Classpath Scanning:** Reads all `V*.sql` scripts in `classpath:db/migration`.
3. **Checksum Verification:** Computes the SHA-256/CRC32 checksum of each script and compares it with the checksum in `flyway_schema_history`.
   - *If a previously executed script was modified, Flyway aborts startup immediately with a checksum validation failure.*
4. **Execution:** Applies pending scripts sequentially inside database transactions.
5. **Lock Release:** Updates the history table and releases the table lock.

---

## 6. Alternatives
| Tool | Format | Pros | Cons |
| :--- | :--- | :--- | :--- |
| **Flyway** | Pure SQL | Easy to write, zero learning curve, full native SQL power | Rollbacks require manual reverse scripts in open-source version |
| **Liquibase** | XML, YAML, JSON, SQL | Built-in automated rollbacks, database-agnostic changesets | Verbose XML/YAML DSL, steeper learning curve |
| **Hibernate `ddl-auto`** | Automatic Java reflection | Convenient for toy prototypes | Extremely dangerous in production, unpredictable schema drift |

---

## 7. Trade-offs
### Advantages:
- **Zero Surprises:** What you write in SQL is exactly what runs on PostgreSQL.
- **Multi-Pod Safe:** Distributed locking ensures only one container runs migrations during blue-green or rolling deployments.

### Limitations:
- **Immutability Rule:** Once a migration script is committed and executed on any shared environment, it can **NEVER** be edited in-place. Changes require a new forward migration (`V3__...sql`).

---

## 8. Common Mistakes
1. **Editing an Existing Migration Script:** Changing a line in `V1__init_schema.sql` after it has run produces `FlywayValidateException: Migration checksum mismatch for migration version 1`. Always create `V3__new_change.sql` instead.
2. **Mixing DDL and Large Data Migrations:** Running a table alter and a 10-million-row update in the same migration script can lock production tables for hours. Separate schema DDL from asynchronous batch data updates.
3. **Leaving `ddl-auto=update` Active:** Running Flyway while leaving Hibernate `ddl-auto=update` leads to conflicting modifications. Always set `ddl-auto=validate` when using Flyway.

---

## 9. Interview Questions & Answers

### Q1: What happens if someone modifies a Flyway migration file that was already executed?
**Answer:** On the next startup, Flyway recomputes the checksum of the modified file and compares it against the checksum stored in `flyway_schema_history`. Because the hashes don't match, Flyway throws `FlywayValidateException` and halts application startup to prevent inconsistent state across environments. To fix this, you must revert the file and create a new forward migration script (or run `flyway:repair` if permitted in local dev).

### Q2: How do you achieve Zero-Downtime Database Migrations in production?
**Answer:** By using the **Expand and Contract (Parallel Run)** pattern across multiple deployment phases:
1. **Phase 1 (Expand):** Add the new column or table as nullable via Flyway (`V3__add_new_column.sql`). Deploy code that writes to both old and new columns.
2. **Phase 2 (Backfill):** Run an asynchronous background job to populate the new column for historical rows.
3. **Phase 3 (Contract):** Deploy code that reads exclusively from the new column.
4. **Phase 4 (Cleanup):** Run a subsequent migration (`V4__drop_old_column.sql`) to remove the deprecated column.

### Q3: When would you choose Liquibase over Flyway?
**Answer:** Choose Liquibase when you need database-agnostic schema changes (e.g., distributing software that must run on both Oracle and PostgreSQL from the same codebase) or when you require automated rollbacks defined in XML/YAML changelogs without writing manual undo SQL scripts.
