# Senior Java & Spring Boot Backend Engineering Interview Master Guide

This guide compiles high-frequency, rigorous technical interview questions and deep-dive answers based on the architectural concepts implemented in the **SATTAEES** production reference platform.

---

## 1. Spring Framework & Spring Boot Internals

### Q1: How does Spring Boot Auto-Configuration work under the hood?
**Answer:**
Auto-configuration is activated by `@EnableAutoConfiguration` (included in `@SpringBootApplication`).
1. **Scanning Candidate Configurations:** Spring loads `org.springframework.boot.autoconfigure.AutoConfiguration.imports` from `META-INF/spring/` on the classpath.
2. **Conditional Evaluation:** Each configuration class uses `@Conditional` annotations to evaluate whether it should execute:
   - `@ConditionalOnClass`: Checks if a specific class is present on the classpath (e.g., `RedisConnectionFactory.class` in `spring-boot-starter-data-redis`).
   - `@ConditionalOnMissingBean`: Only registers a default bean if the developer has not declared their own custom bean.
   - `@ConditionalOnProperty`: Checks configuration properties in `application.properties`.
3. **Execution Order:** Auto-configurations are ordered using `@AutoConfigureAfter` or `@AutoConfigureBefore` to ensure proper dependency resolution.

### Q2: What is the Bean Lifecycle in Spring?
**Answer:**
1. **Instantiation:** Spring creates an instance using reflection via the bean's constructor.
2. **Populate Properties:** Dependencies are injected (Setter/Field/Constructor injection).
3. **BeanNameAware / BeanFactoryAware / ApplicationContextAware:** Injects internal Spring references.
4. **BeanPostProcessor (Pre-Initialization):** `postProcessBeforeInitialization()` runs on all registered processors.
5. **Initialization:**
   - `@PostConstruct` method executes.
   - `InitializingBean.afterPropertiesSet()` runs.
   - Custom `initMethod` declared in `@Bean(initMethod = "...")`.
6. **BeanPostProcessor (Post-Initialization):** `postProcessAfterInitialization()` creates AOP dynamic proxies (for `@Transactional`, `@Async`, `@Cacheable`).
7. **Ready for Use:** Bean serves application requests.
8. **Destruction:** On container shutdown, `@PreDestroy`, `DisposableBean.destroy()`, and custom `destroyMethod` execute.

---

## 2. Spring Security & JWT Architecture

### Q3: Why is Constructor Injection preferred over Field Injection (`@Autowired`)?
**Answer:**
1. **Immutability:** Dependencies can be declared `final`, ensuring thread safety and preventing modification after initialization.
2. **Easy Unit Testing:** Classes can be instantiated in plain JUnit tests using `new MyService(mockRepo)` without starting a Spring test context or using reflection.
3. **Prevention of Circular Dependencies:** Spring fails fast at application startup if a circular dependency exists between constructors.
4. **Null Safety:** Guarantees that an instance cannot be created with missing dependencies.

### Q4: Explain the difference between Authentication and Authorization in Spring Security.
**Answer:**
- **Authentication:** Verifying *who you are* (e.g. verifying email and BCrypt password hash in `AuthController.login()`, returning a JWT token).
- **Authorization:** Verifying *what you are allowed to do* (e.g., checking if the authenticated user has `ROLE_CUSTOMER` or owns the specific resource being modified via `@PreAuthorize`).

### Q5: How do you secure against Insecure Direct Object References (IDOR)?
**Answer:**
IDOR happens when a user modifies a URL parameter (e.g., `PUT /api/job-requests/105/cancel`) to alter another user's record. Prevention in Sattaees:
1. Extract the authenticated user ID from the JWT principal.
2. In `JobRequestService`, verify that the entity's `customer.id` or `worker.id` matches the authenticated principal:
   ```java
   if (!job.getCustomer().getId().equals(currentUser.getId())) {
       throw new AccessDeniedException("You do not have permission to modify this job.");
   }
   ```

---

## 3. Data Persistence, JPA & Concurrency

### Q6: What is the difference between `@Transactional(readOnly = true)` and standard `@Transactional`?
**Answer:**
- When `readOnly = true` is set:
  1. **Hibernate Optimization:** Hibernate sets FlushMode to `MANUAL` and disables Dirty Checking snapshot creation, saving significant memory and CPU cycles.
  2. **JDBC / DB Level:** Many database drivers (e.g., PostgreSQL) will route read-only transactions to read replicas when configured in a primary-replica cluster.
  3. **Data Safety:** Prevents accidental entity mutations within read operations.

### Q7: What are the isolation levels in SQL transactions and what anomalies do they prevent?
**Answer:**
1. **READ UNCOMMITTED:** Prevents nothing; allows Dirty Reads, Non-Repeatable Reads, and Phantom Reads.
2. **READ COMMITTED:** Prevents Dirty Reads (a query only sees committed data). Default in PostgreSQL.
3. **REPEATABLE READ:** Prevents Dirty Reads and Non-Repeatable Reads (reading the same row twice inside a transaction always returns the exact same data).
4. **SERIALIZABLE:** Prevents all anomalies by enforcing complete sequential execution equivalence.

### Q8: What is the difference between Optimistic Locking and Pessimistic Locking?
**Answer:**
- **Optimistic Locking:** Assumes conflict is rare. Uses a `@Version` column. No database locks are held. If a collision occurs on commit, `OptimisticLockingFailureException` is thrown.
- **Pessimistic Locking:** Assumes conflict is frequent. Executes `SELECT ... FOR UPDATE`, holding an exclusive database row lock until the transaction finishes. Blocks other readers/writers.

---

## 4. Caching & Performance (Redis)

### Q9: What is the Thundering Herd (Cache Stampede) problem and how do you mitigate it?
**Answer:**
When a heavily accessed key expires, thousands of concurrent requests miss the cache simultaneously and all query the database at once, overwhelming CPU and connection pools.
**Mitigations:**
1. **Distributed Mutex Lock:** Use Redis `SET key val NX EX 10` so only one thread recomputes the cache while others wait or return a stale copy.
2. **Probabilistic Early Expiration (XFetch):** Recompute cache asynchronously in the background before the key expires based on traffic probability.
3. **Soft TTLs:** Store two timestamps in the cache value: `expiration` and `softExpiration`. If accessed past soft expiration, return stale data while triggering a background refresh.

---

## 5. Message Brokers & Asynchronous Processing (Kafka)

### Q10: What is the difference between At-Least-Once, At-Most-Once, and Exactly-Once delivery?
**Answer:**
- **At-Most-Once:** Offsets committed before processing. If consumer crashes during processing, message is lost. Zero duplicates.
- **At-Least-Once:** Message processed first, offset committed second. If consumer crashes before commit, message is re-delivered. Possible duplicates (handled via idempotent consumer).
- **Exactly-Once (EOS):** Uses Kafka transactional producers and `read_committed` consumer isolation to ensure messages and state stores update atomically.

### Q11: How do you handle Poison Pill messages in Kafka?
**Answer:**
A poison pill is a message that repeatedly fails deserialization or business logic (e.g. malformed JSON). Naive setups will retry infinitely, blocking partition consumption.
**Solution in Sattaees:**
Configure `DefaultErrorHandler` with a `DeadLetterPublishingRecoverer`. After a fixed number of retries (e.g. 3 attempts), the error handler automatically routes the failing message to a dead-letter topic (`<topic>.DLT`) with error headers, commits the offset on the primary topic, and allows normal processing to proceed.

---

## 6. System Design Scenario: Marketplace Booking

### Scenario: Design a high-concurrency labor booking platform (like Sattaees) that handles 50,000 bookings per minute during peak hours.
**Architecture Blueprint:**
1. **Read Path:**
   - CDN (Cloudflare) caches static assets.
   - Worker search queries hit a Redis cluster configured with Cache-Aside and key TTLs.
   - Read replicas for PostgreSQL serve search misses.
2. **Write Path (Booking):**
   - Inbound requests authenticate via stateless JWT tokens.
   - Optimistic locking (`@Version`) prevents race conditions during worker status updates.
   - Primary PostgreSQL database processes ACID booking transactions.
   - Kafka decouples notifications and alerts from the booking HTTP thread.
3. **Resilience & Scaling:**
   - Stateless Spring Boot pods scale horizontally in Kubernetes (HPA based on CPU / Actuator metrics).
   - Redis cluster for shared cache with `LoggingCacheErrorHandler` fallback.
   - Kafka partition scaling for parallel consumer processing.
