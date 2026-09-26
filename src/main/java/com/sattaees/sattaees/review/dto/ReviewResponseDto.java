package com.sattaees.sattaees.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {

    private Long id;
    private Long workerId;
    private String workerName;
    private Long customerId;
    private String customerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    // Nested wrappers for backwards compatibility with frontend
    private EntityRef worker;
    private EntityRef customer;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EntityRef {
        private Long id;
        private String name;
    }
}
