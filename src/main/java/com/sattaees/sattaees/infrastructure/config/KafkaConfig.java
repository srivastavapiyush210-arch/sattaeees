package com.sattaees.sattaees.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Apache Kafka event-driven streaming configuration with Dead Letter Topic (DLT)
 * and retry strategy for resilient asynchronous processing.
 */
@Slf4j
@EnableKafka
@Configuration
public class KafkaConfig {

    public static final String TOPIC_JOB_EVENTS = "sattaees.job-events";
    public static final String TOPIC_REVIEW_EVENTS = "sattaees.review-events";

    @Value("${app.kafka.topics.job-events:" + TOPIC_JOB_EVENTS + "}")
    private String jobEventsTopic;

    @Value("${app.kafka.topics.review-events:" + TOPIC_REVIEW_EVENTS + "}")
    private String reviewEventsTopic;

    @Bean
    public NewTopic jobEventsTopic() {
        return TopicBuilder.name(jobEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reviewEventsTopic() {
        return TopicBuilder.name(reviewEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Resilient error handler for Kafka consumers:
     * Retries failed messages with exponential backoff (3 attempts),
     * and publishes persistent failures to a Dead Letter Topic (e.g. <topic>.DLT).
     */
    @Bean
    @SuppressWarnings("unchecked")
    public CommonErrorHandler kafkaErrorHandler(@org.springframework.beans.factory.annotation.Autowired(required = false) KafkaOperations<?, ?> kafkaOperations) {
        if (kafkaOperations != null) {
            DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer((KafkaOperations<Object, Object>) kafkaOperations);
            ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
            backOff.setMaxAttempts(3);

            DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
            errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
                log.warn("Kafka Consumer Retry attempt #{} for topic: {}, key: {}. Reason: {}",
                        deliveryAttempt, record.topic(), record.key(), ex.getMessage());
            });
            return errorHandler;
        }
        return new DefaultErrorHandler();
    }
}
