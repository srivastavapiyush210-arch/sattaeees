# ADR-004: Redis Cache-Aside Pattern with Fault-Tolerant Fallback

## Status
**Accepted**

## Context
Worker profiles, search listings, and aggregate ratings are queried heavily by customers exploring labor options. Relational queries filtering by skill and city, combined with profile lookups, place unnecessary read pressure on PostgreSQL.

However, caching introduces two classic risks:
1. **Stale Data:** If a worker's phone number or hourly rate is updated or a new review is posted, customers must not view obsolete contact or rating info.
2. **Cascading System Failure:** If Redis crashes or undergoes maintenance, the backend application must not crash or return HTTP 500 errors to customers.

## Decision
We implemented the **Cache-Aside (Lazy Loading)** pattern using Spring's Cache abstraction backed by **Redis**:

1. **Selective Caching:**
   - `workers:search`: All workers list (TTL: 10 minutes).
   - `workers:profile`: Individual worker profile by ID (TTL: 60 minutes).
2. **Explicit Cache Invalidation:**
   - Synchronous `@CacheEvict` in `WorkerService.updateWorker()`, `WorkerService.deleteWorker()`, and `ReviewService.createReview()`.
3. **Resilient Fallback via `LoggingCacheErrorHandler`:**
   - Implemented custom `CacheErrorHandler` that traps `RedisConnectionFailureException` on `handleCacheGetError`, `handleCachePutError`, and `handleCacheEvictError`.
   - Logs a `WARN` event and allows normal execution to fall through to PostgreSQL without failing the user's HTTP request.

## Consequences

### Positive:
- **Sub-Millisecond Read Latency:** Worker profile lookups served from Redis in ~1-2ms compared to ~15-30ms database roundtrips.
- **High Availability & Fault Tolerance:** If Redis is down, the system automatically degrades to direct database queries without user-facing outages.
- **Cache Consistency:** Invalidation on updates and reviews prevents stale data from persisting beyond the write transaction.

### Negative:
- Adds an infrastructure dependency (Redis) to the operational footprint.
- Memory consumption must be monitored in production using Redis maxmemory policies (`allkeys-lru`).

## Alternatives Considered
- **In-Memory Cache (Caffeine / Guava):** Simpler to configure with zero external dependencies, but cannot be shared across multiple backend pods when scaling horizontally, causing cache inconsistency between instances.
- **No Caching:** Simple, but unscalable under peak morning booking spikes.
