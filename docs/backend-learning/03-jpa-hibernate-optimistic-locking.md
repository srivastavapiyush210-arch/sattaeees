# Backend Engineering Guide: JPA, Hibernate, Concurrency & Optimistic Locking

## 1. What Problem It Solves
Relational databases operate on tables, rows, and foreign keys; object-oriented languages operate on classes, objects, and references (the **Object-Relational Impedance Mismatch**). Writing manual JDBC code for every CRUD operation leads to thousands of lines of boilerplate SQL, manual result set parsing, and difficult transaction management.

Furthermore, multi-user web applications suffer from **Concurrency Collisions (Lost Updates)**: two users reading the same record at the same time and submitting conflicting updates.

**JPA (Jakarta Persistence API) & Hibernate** provide object-relational mapping, dirty checking, and transaction orchestration, while **Optimistic Locking** ensures data integrity during concurrent writes without holding database locks.

---

## 2. Why Sattaees Needs It
Sattaees handles concurrent marketplace interactions:
- Multiple customers may attempt to book the same worker simultaneously.
- A worker and customer might attempt conflicting status transitions on a job request (e.g. customer cancels while worker accepts).
- Customers submit reviews that aggregate into the worker's `average_rating` and `total_reviews`.
- Loading a customer's job list requires fetching associated `Customer` and `Worker` details without causing 50 separate SQL queries (**N+1 Query Problem**).

---

## 3. How It Works

### Entity Inheritance & Optimistic Locking
Sattaees defines a shared `@MappedSuperclass` with automatic auditing and a `@Version` field:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditableEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
```

### Optimistic Lock Execution in SQL:
When a service updates a `JobRequest`:
1. Hibernate issues:
   ```sql
   UPDATE job_requests 
   SET status = 'ACCEPTED', version = 2, updated_at = NOW() 
   WHERE id = 105 AND version = 1;
   ```
2. If another transaction changed `version` to 2 in the meantime, the `WHERE` condition matches 0 rows.
3. Hibernate detects that 0 rows were updated and throws `OptimisticLockException`.
4. Spring wraps it in `OptimisticLockingFailureException`, and Sattaees' `GlobalExceptionHandler` converts it to HTTP 409 Conflict.

---

## 4. How It Integrates with Spring Boot
- **Spring Data JPA:** Generates dynamic DAO implementations at runtime based on repository interfaces (`JobRequestRepository extends JpaRepository<JobRequest, Long>`).
- **Transaction Management:** `@Transactional` opens a database connection from HikariCP, binds it to the current thread, executes operations, flushes the persistence context, and commits (or rolls back on `RuntimeException`).
- **N+1 Prevention with `@EntityGraph`:**
  ```java
  @EntityGraph(attributePaths = {"customer", "worker"})
  List<JobRequest> findByWorkerId(Long workerId);
  ```
  Generates a single SQL `LEFT OUTER JOIN` instead of firing 1 query for the jobs + N queries for workers/customers.

---

## 5. What Happens Internally

### The Persistence Context (First-Level Cache)
1. Within a `@Transactional` boundary, Hibernate maintains an `EntityManager` and a **Persistence Context**.
2. When `findById(id)` is called, Hibernate checks its first-level cache. If present, it returns the existing Java reference without executing SQL.
3. **Dirty Checking:** When an entity is loaded, Hibernate stores a snapshot of its initial state. At transaction commit time, Hibernate compares the entity's current state with the snapshot. If any field changed, Hibernate automatically generates and executes an `UPDATE` statement. You do NOT need to call `repository.save()` on managed entities!

---

## 6. Alternatives
| Technology | Paradigm | Pros | Cons |
| :--- | :--- | :--- | :--- |
| **Spring Data JPA / Hibernate** | Full ORM | High developer productivity, dirty checking, portability | Learning curve, risk of N+1 queries, heavy abstractions |
| **jOOQ** | Typesafe SQL builder | Total control over SQL, zero magic, typesafe DSL | Manual mapping, no dirty checking |
| **MyBatis** | SQL Mapper | Full control over complex queries | Verbose XML/annotations, high maintenance |
| **Spring JdbcClient / JdbcTemplate** | Low-level SQL | Fast, lightweight, zero overhead | High boilerplate, manual mapping |

---

## 7. Trade-offs: Optimistic vs. Pessimistic Locking
| Characteristic | Optimistic Locking (`@Version`) | Pessimistic Locking (`SELECT FOR UPDATE`) |
| :--- | :--- | :--- |
| **Database Locks Held** | None | Exclusive row lock held for entire transaction |
| **Concurrency & Throughput** | Very High (reads never block) | Lower (concurrent reads/writes are queued) |
| **Conflict Frequency** | Best for low-to-medium collision rates | Best for high collision rates (e.g. flash sales) |
| **Failure Mode** | Throws exception on commit | Blocks waiting for lock timeout; risk of deadlocks |

---

## 8. Common Mistakes
1. **The N+1 Query Problem:** Fetching a list of 100 entities with `@ManyToOne(fetch = FetchType.LAZY)` and accessing the relationship in a loop generates 101 SQL queries. Always use `@EntityGraph` or `JOIN FETCH`.
2. **Calling `@Transactional` from Within the Same Class (Self-Invocation):** Spring uses dynamic AOP proxies. Calling `this.updateStatus()` bypasses the proxy, meaning NO transaction is started!
3. **Catching Exceptions and Not Re-throwing:** Catching an exception inside `@Transactional` without re-throwing prevents Spring from rolling back the transaction.
4. **Missing `@Version` on High-Concurrency Entities:** Leaving entities without `@Version` causes lost updates when two users edit the same entity simultaneously.

---

## 9. Interview Questions & Answers

### Q1: What is the N+1 Query Problem and how do you resolve it in Spring Data JPA?
**Answer:** The N+1 problem occurs when querying for N parent entities results in 1 initial query for the parents, followed by N separate queries to fetch the lazy child associations when accessed in code. It is solved by using:
1. `@EntityGraph(attributePaths = {"associationName"})` in Spring Data repositories.
2. Explicit `JOIN FETCH p.association` in JPQL.
3. Batch fetching (`@BatchSize(size = 25)` or `spring.jpa.properties.hibernate.default_batch_fetch_size=25`).

### Q2: How does Hibernate's Dirty Checking mechanism work?
**Answer:** When an entity is loaded into the Persistence Context (First-Level Cache), Hibernate creates an internal copy (snapshot) of its property values. During the `flush()` phase before transaction commit, Hibernate compares the entity's current field values against the snapshot. For every modified property, Hibernate dynamically generates an `UPDATE` statement and queues it in the action queue.

### Q3: Why should you avoid `FetchType.EAGER` on `@OneToMany` and `@ManyToOne`?
**Answer:** `FetchType.EAGER` forces Hibernate to load associations immediately on every query, even when the business logic doesn't need them. On `@ManyToOne`, it often defaults to EAGER in JPA specifications, triggering cascading joins and massive memory bloat. Best practice is to set all relationships to `FetchType.LAZY` and explicitly fetch them using `@EntityGraph` when needed.
