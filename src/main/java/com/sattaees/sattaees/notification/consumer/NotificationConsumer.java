package com.sattaees.sattaees.notification.consumer;

import com.sattaees.sattaees.infrastructure.config.KafkaConfig;
import com.sattaees.sattaees.notification.event.JobEvent;
import com.sattaees.sattaees.notification.event.ReviewEvent;
import com.sattaees.sattaees.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Asynchronous Kafka event consumer with consumer group load balancing
 * and idempotency deduplication.
 */
@Slf4j
@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_JOB_EVENTS,
            groupId = "${spring.kafka.consumer.group-id:sattaees-notification-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeJobEvent(JobEvent event) {
        if (event == null || event.getEventId() == null) {
            log.warn("Received invalid or null JobEvent from Kafka");
            return;
        }

        // Idempotency check: discard already-processed event IDs
        if (notificationService.isEventProcessed(event.getEventId())) {
            log.info("Duplicate JobEvent detected (eventId: {}). Discarding.", event.getEventId());
            return;
        }

        log.info("Kafka consumer received JobEvent: {} (eventId: {})", event.getEventType(), event.getEventId());
        notificationService.sendJobNotification(event);
        notificationService.markEventProcessed(event.getEventId());
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_REVIEW_EVENTS,
            groupId = "${spring.kafka.consumer.group-id:sattaees-notification-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeReviewEvent(ReviewEvent event) {
        if (event == null || event.getEventId() == null) {
            log.warn("Received invalid or null ReviewEvent from Kafka");
            return;
        }

        if (notificationService.isEventProcessed(event.getEventId())) {
            log.info("Duplicate ReviewEvent detected (eventId: {}). Discarding.", event.getEventId());
            return;
        }

        log.info("Kafka consumer received ReviewEvent for worker: {} (eventId: {})", event.getWorkerId(), event.getEventId());
        notificationService.sendReviewNotification(event);
        notificationService.markEventProcessed(event.getEventId());
    }
}
