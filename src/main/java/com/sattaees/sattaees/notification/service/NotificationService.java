package com.sattaees.sattaees.notification.service;

import com.sattaees.sattaees.notification.event.JobEvent;
import com.sattaees.sattaees.notification.event.ReviewEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Asynchronous notification service simulating SMS, Email, and Push alerts
 * with built-in idempotency tracking.
 */
@Slf4j
@Service
public class NotificationService {

    // Idempotency cache: tracks processed event IDs to prevent duplicate notifications
    private final Set<String> processedEvents = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public boolean isEventProcessed(String eventId) {
        return processedEvents.contains(eventId);
    }

    public void markEventProcessed(String eventId) {
        processedEvents.add(eventId);
    }

    public void sendJobNotification(JobEvent event) {
        log.info("[NOTIFICATION] Event: {} | Job ID: {} | Service: {} | Status: {}",
                event.getEventType(), event.getJobId(), event.getServiceType(), event.getStatus());

        switch (event.getEventType()) {
            case "JOB_CREATED" -> {
                log.info("📧 [Email -> Worker {}]: New booking request for '{}' from customer {}",
                        event.getWorkerEmail(), event.getServiceType(), event.getCustomerName());
                log.info("📱 [Push -> Customer {}]: Your request for '{}' was submitted successfully.",
                        event.getCustomerEmail(), event.getServiceType());
            }
            case "JOB_ACCEPTED" -> {
                log.info("📱 [SMS -> Customer {}]: Worker {} has ACCEPTED your job request #{}!",
                        event.getCustomerEmail(), event.getWorkerName(), event.getJobId());
            }
            case "JOB_IN_PROGRESS" -> {
                log.info("📱 [Push -> Customer {}]: Worker {} has started the job #{} (IN_PROGRESS).",
                        event.getCustomerEmail(), event.getWorkerName(), event.getJobId());
            }
            case "JOB_COMPLETED" -> {
                log.info("📧 [Email -> Customer {}]: Job #{} marked COMPLETED! Please leave a review for {}.",
                        event.getCustomerEmail(), event.getJobId(), event.getWorkerName());
            }
            case "JOB_CANCELLED" -> {
                log.info("⚠️ [Alert]: Job request #{} was CANCELLED.", event.getJobId());
            }
            default -> log.info("Processed job event: {}", event.getEventType());
        }
    }

    public void sendReviewNotification(ReviewEvent event) {
        log.info("⭐ [Review Notification]: Worker ID: {} received a {}-star review! Comment: {}",
                event.getWorkerId(), event.getRating(), event.getComment());
    }
}
