package com.sattaees.sattaees.review.controller;

import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import com.sattaees.sattaees.review.dto.CreateReviewDto;
import com.sattaees.sattaees.review.dto.ReviewResponseDto;
import com.sattaees.sattaees.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Endpoints for submitting and viewing customer ratings and feedback")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "Submit a review for a worker (Supports JSON body and query parameters)")
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestBody(required = false) @Valid CreateReviewDto bodyDto,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        CreateReviewDto request = (bodyDto != null) ? bodyDto : CreateReviewDto.builder()
                .customerId(customerId)
                .workerId(workerId)
                .rating(rating)
                .comment(comment)
                .build();

        log.info("REST request to leave review: workerId={}, rating={}", request.getWorkerId(), request.getRating());
        ReviewResponseDto response = reviewService.createReview(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/worker/{workerId}")
    @Operation(summary = "Get all reviews submitted for a specific worker")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsForWorker(@PathVariable Long workerId) {
        log.info("REST request to get reviews for worker ID: {}", workerId);
        return ResponseEntity.ok(reviewService.getReviewsForWorker(workerId));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all reviews submitted by a specific customer")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsForCustomer(@PathVariable Long customerId) {
        log.info("REST request to get reviews by customer ID: {}", customerId);
        return ResponseEntity.ok(reviewService.getReviewsForCustomer(customerId));
    }
}
