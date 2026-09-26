package com.sattaees.sattaees.review.mapper;

import com.sattaees.sattaees.review.dto.ReviewResponseDto;
import com.sattaees.sattaees.review.entity.Review;

public final class ReviewMapper {

    private ReviewMapper() {}

    public static ReviewResponseDto toResponseDto(Review review) {
        if (review == null) {
            return null;
        }

        Long workerId = (review.getWorker() != null) ? review.getWorker().getId() : null;
        String workerName = (review.getWorker() != null) ? review.getWorker().getName() : null;

        Long customerId = (review.getCustomer() != null) ? review.getCustomer().getId() : null;
        String customerName = (review.getCustomer() != null) ? review.getCustomer().getName() : null;

        return ReviewResponseDto.builder()
                .id(review.getId())
                .workerId(workerId)
                .workerName(workerName)
                .customerId(customerId)
                .customerName(customerName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .worker(workerId != null ? new ReviewResponseDto.EntityRef(workerId, workerName) : null)
                .customer(customerId != null ? new ReviewResponseDto.EntityRef(customerId, customerName) : null)
                .build();
    }
}
