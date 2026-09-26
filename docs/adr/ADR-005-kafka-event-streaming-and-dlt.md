# ADR-005: Event Streaming with Apache Kafka and Dead Letter Topics

## Status
**Accepted**

## Context
When jobs are created, accepted, completed, or reviewed, multiple side-effects must occur:
- Notifying the assigned worker via SMS or push alert.
- Alerting the customer when work starts or finishes.
- Emitting audit logs and analytical metrics.

Executing these downstream actions synchronously in the HTTP request thread creates high tail latency, risks connection pool exhaustion, and causes transaction rollbacks if external notification APIs fail.

## Decision
We adopted **Apache Kafka** (running in lightweight KRaft mode without ZooKeeper) for asynchronous, decoupled event-driven workflows:

1. **Domain Events:**
   - `JobEvent` emitted to `sattaees.job.events` on state transitions (`CREATED`, `ACCEPTED`, `COMPLETED`, `CANCELLED`).
   - `ReviewEvent` emitted to `sattaees.review.events` when ratings are submitted.
2. **Consumer Group & Resiliency:**
   - Dedicated consumer group `sattaees-notification-group` processes events concurrently.
   - Configured `DefaultErrorHandler` with 3 retries (1-second backoff) using `DeadLetterPublishingRecoverer` to route poisoned messages to `<topic>.DLT`.
3. **Consumer Idempotency:**
   - Every event embeds a unique `eventId` (UUID).
   - Consumers deduplicate incoming messages against a processed set to handle Kafka's at-least-once delivery guarantee without sending duplicate alerts.

## Consequences

### Positive:
- **Zero HTTP Latency Overhead:** Job creation transactions commit immediately to PostgreSQL and return `201 Created` to the client in ~20ms without waiting for notifications.
- **Fault Isolation:** Even if SMS/email gateways or notification consumers are offline, customer job bookings succeed and events buffer safely in Kafka topics.
- **Dead-Letter Auditability:** Malformed payloads or persistent failures route to `.DLT` for inspection without stalling the consumer pipeline.

### Negative:
- Increases operational complexity with broker management, topic provisioning, and serialization schemas.
- Eventual consistency requires careful UX design (the user receives immediate booking confirmation while worker notification occurs asynchronously).

## Alternatives Considered
- **Spring `@Async` / In-Memory Thread Pool:** Zero external infrastructure, but events are lost if the JVM restarts or crashes during processing. Lacks consumer groups and durability.
- **RabbitMQ:** Excellent for lightweight AMQP task queues, but lacks Kafka's high-throughput event log replayability, retention, and partition scaling for future analytics.
