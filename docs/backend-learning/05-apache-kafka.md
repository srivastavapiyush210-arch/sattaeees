# Backend Engineering Guide: Apache Kafka & Event-Driven Architecture

## 1. What Problem It Solves
When a customer creates a job booking in a marketplace, multiple actions must happen:
1. Save the job to the database.
2. Send an SMS alert to the assigned worker.
3. Send an email confirmation to the customer.
4. Update analytics and audit logs.

If done **synchronously in the HTTP thread**:
- Calling third-party SMS/email APIs adds 500ms–2000ms latency to the user's request.
- If the SMS gateway is down, the user's booking fails and rolls back the database transaction.
- If traffic spikes 10x, external APIs rate-limit or crash the backend thread pool.

**Apache Kafka** solves this by acting as a distributed, high-throughput, fault-tolerant **Event Streaming Log** that decouples the primary transaction from downstream asynchronous side-effects.

---

## 2. Why Sattaees Needs It
Sattaees uses Kafka for domain events:
- **`JobEvent`** (`JOB_CREATED`, `JOB_ACCEPTED`, `JOB_COMPLETED`, `JOB_CANCELLED`): Dispatched to `sattaees.job.events` when job status changes.
- **`ReviewEvent`**: Dispatched to `sattaees.review.events` when ratings are posted.
- **`NotificationConsumer`**: Listens to these events and alerts workers and customers asynchronously without delaying HTTP responses.
- **Dead Letter Topics (`.DLT`)**: Isolate poisoned messages without breaking consumer pipelines.

---

## 3. How It Works: Concepts & Topologies
- **Topic:** An append-only category or feed to which records are published (e.g. `sattaees.job.events`).
- **Partition:** Topics are divided into partitions for parallelism across brokers. Within a partition, messages have strict ordering identified by an **Offset**.
- **Producer:** Sattaees publishes events using `KafkaTemplate<String, Object>`.
- **Consumer Group:** Multiple consumers with the same `group-id` (`sattaees-notification-group`) share partition consumption. Adding consumer instances scales throughput horizontally.

```mermaid
flowchart LR
    P[JobRequestService Producer] -->|Send JobEvent| T[Topic: sattaees.job.events]
    subgraph KAFKA [Kafka Partitioned Log]
        T --> Part0[Partition 0]
        T --> Part1[Partition 1]
    end
    Part0 --> C1[Consumer 1 (Worker Thread)]
    Part1 --> C2[Consumer 2 (Worker Thread)]
    C1 -->|3 Retries Failed| DLT[Topic: sattaees.job.events.DLT]
```

---

## 4. How It Integrates with Spring Boot
1. **Dependency:** `spring-kafka` provides auto-configuration and Spring idioms.
2. **Publishing:**
   ```java
   kafkaTemplate.send("sattaees.job.events", String.valueOf(job.getId()), jobEvent);
   ```
3. **Consuming:**
   ```java
   @KafkaListener(topics = "sattaees.job.events", groupId = "sattaees-notification-group")
   public void onJobEvent(@Payload JobEvent event) {
       notificationService.handleJobEvent(event);
   }
   ```
4. **Resilience with Dead Letter Topic (DLT):**
   ```java
   DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
   DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
   factory.setCommonErrorHandler(errorHandler);
   ```

---

## 5. What Happens Internally: Why Kafka Is So Fast
1. **Append-Only Sequential Disk I/O:** Kafka writes records sequentially to the end of partition commit logs. Sequential disk I/O on modern SSDs/HDDs is as fast as random memory access (~600 MB/sec).
2. **Page Cache Architecture:** Kafka writes data to the OS page cache (kernel memory) rather than maintaining large JVM heap objects, avoiding Java Garbage Collection (GC) pauses.
3. **Zero-Copy Data Transfer (`sendfile`):** When consumers read data, Kafka transfers bytes directly from OS Page Cache to the network socket using the Linux kernel system call `sendfile()`, completely bypassing JVM user-space memory buffers.

---

## 6. Alternatives
| Broker | Architecture | Best Used For | Sattaees Choice |
| :--- | :--- | :--- | :--- |
| **Apache Kafka** | Distributed commit log, partition-based | High-throughput events, log replay, streaming pipelines | **Selected:** Industry standard for event-driven systems |
| **RabbitMQ** | AMQP message broker with smart routing | Complex routing, RPC, individual message acknowledgment | Great for task queues, less suited for log replay |
| **AWS SQS / SNS** | Managed cloud queue & pub-sub | Cloud-native AWS setups | Vendor lock-in, not portable for local Docker development |
| **Spring `@Async`** | In-memory thread pool | Very simple background tasks | Messages lost if JVM restarts |

---

## 7. Trade-offs
### Advantages:
- **Massive Scalability:** Partitions enable horizontal scale across multiple cluster nodes.
- **Event Replayability:** Consumers can rewind offsets to replay past events (e.g. after fixing a bug or adding a new recommendation service).
- **Fault Isolation:** Slow downstream consumers cannot backpressure or crash the HTTP application.

### Limitations:
- **Eventual Consistency:** Data is not available downstream instantaneously.
- **Operational Overhead:** Managing brokers, topics, and consumer lag requires monitoring tools (Kafka UI, Prometheus).

---

## 8. Common Mistakes
1. **Blocking the HTTP Thread for Kafka ACK:** Calling `kafkaTemplate.send(...).get()` forces synchronous waiting, destroying the entire benefit of asynchronous messaging. Use asynchronous callbacks or fire-and-forget for decoupled events.
2. **Ignoring Idempotency:** Kafka guarantees *at-least-once delivery*. If a consumer dies right before committing its offset, re-delivery occurs. Consumers must be idempotent (e.g., check `eventId` against a processed set).
3. **Poison Pills Halting Consumers:** If a malformed message causes an unhandled exception, naive consumers will retry infinitely, halting the entire partition. Always configure a `DeadLetterPublishingRecoverer`.

---

## 9. Interview Questions & Answers

### Q1: Why is Apache Kafka faster than traditional message brokers like ActiveMQ or RabbitMQ?
**Answer:**
1. **Sequential Disk I/O:** Writes append sequentially to disk logs without B-tree re-indexing.
2. **Kernel Page Cache:** Leverages OS page caching directly rather than caching inside JVM heap memory.
3. **Zero-Copy Network Transfers:** Uses the OS `sendfile` system call to stream bytes directly from OS cache to network sockets without copying into JVM user space.
4. **Batching:** Producers and consumers batch records together, reducing network packet roundtrips.

### Q2: What is a Consumer Group Rebalance and when does it happen?
**Answer:** A rebalance is the process where Kafka reallocates topic partitions among consumer group members. It occurs when:
1. A new consumer joins the group.
2. An existing consumer crashes or leaves.
3. A consumer fails to send heartbeats within `session.timeout.ms`.
4. Topic partitions are added or modified.

### Q3: How do you achieve Idempotent Consumption in Kafka consumers?
**Answer:**
1. Include a globally unique `eventId` or business key (e.g., `jobRequestId + status`) in each event payload.
2. In the consumer, check if the key exists in an atomic deduplication store (Redis `SETNX` or database unique constraint table).
3. If already processed, acknowledge the offset and exit immediately. If new, process the message and record the key inside the same atomic step.
