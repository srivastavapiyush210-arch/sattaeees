package com.sattaees.sattaees.review.service;

import com.sattaees.sattaees.common.exception.InvalidOperationException;
import com.sattaees.sattaees.common.exception.ResourceNotFoundException;
import com.sattaees.sattaees.customer.entity.Customer;
import com.sattaees.sattaees.customer.repository.CustomerRepository;
import com.sattaees.sattaees.infrastructure.config.KafkaConfig;
import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import com.sattaees.sattaees.notification.event.ReviewEvent;
import com.sattaees.sattaees.review.dto.CreateReviewDto;
import com.sattaees.sattaees.review.dto.ReviewResponseDto;
import com.sattaees.sattaees.review.entity.Review;
import com.sattaees.sattaees.review.mapper.ReviewMapper;
import com.sattaees.sattaees.review.repository.ReviewRepository;
import com.sattaees.sattaees.worker.entity.Worker;
import com.sattaees.sattaees.worker.service.WorkerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service managing reviews, updating worker ratings atomically via WorkerService,
 * and publishing Kafka review events.
 */
@Slf4j
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final WorkerService workerService;
    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReviewService(ReviewRepository reviewRepository,
                         WorkerService workerService,
                         CustomerRepository customerRepository,
                         KafkaTemplate<String, Object> kafkaTemplate) {
        this.reviewRepository = reviewRepository;
        this.workerService = workerService;
        this.customerRepository = customerRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public ReviewResponseDto createReview(CreateReviewDto dto, UserPrincipal currentUser) {
        final Long finalCustomerId = (dto.getCustomerId() != null) ? dto.getCustomerId()
                : (currentUser != null && "CUSTOMER".equalsIgnoreCase(currentUser.getRole()) ? currentUser.getId() : null);

        if (finalCustomerId == null) {
            throw new InvalidOperationException("Customer ID is required to leave a review.");
        }
        if (dto.getWorkerId() == null) {
            throw new InvalidOperationException("Worker ID is required to leave a review.");
        }

        log.info("Creating review for worker ID: {} by customer ID: {} (rating: {})",
                dto.getWorkerId(), finalCustomerId, dto.getRating());

        Customer customer = customerRepository.findById(finalCustomerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + finalCustomerId));

        Worker worker = workerService.findWorkerEntityById(dto.getWorkerId());

        Review review = Review.builder()
                .worker(worker)
                .customer(customer)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Atomically recompute worker rating and evict Redis cache
        workerService.updateWorkerRating(worker.getId(), dto.getRating());

        // Publish Kafka event asynchronously
        publishReviewEvent(savedReview);

        return ReviewMapper.toResponseDto(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsForWorker(Long workerId) {
        log.debug("Fetching reviews for worker ID: {} (N+1 prevented)", workerId);
        return reviewRepository.findByWorkerId(workerId).stream()
                .map(ReviewMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsForCustomer(Long customerId) {
        log.debug("Fetching reviews for customer ID: {} (N+1 prevented)", customerId);
        return reviewRepository.findByCustomerId(customerId).stream()
                .map(ReviewMapper::toResponseDto)
                .toList();
    }

    private void publishReviewEvent(Review review) {
        try {
            ReviewEvent event = ReviewEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("REVIEW_SUBMITTED")
                    .reviewId(review.getId())
                    .workerId(review.getWorker().getId())
                    .customerId(review.getCustomer().getId())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(KafkaConfig.TOPIC_REVIEW_EVENTS, String.valueOf(review.getWorker().getId()), event);
            log.info("Dispatched Kafka ReviewEvent for review ID: {}", review.getId());
        } catch (Exception ex) {
            log.warn("Failed to publish Kafka review event for review ID: {}. Non-fatal warning: {}", review.getId(), ex.getMessage());
        }
    }
}
