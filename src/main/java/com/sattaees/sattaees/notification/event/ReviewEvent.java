package com.sattaees.sattaees.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEvent implements Serializable {

    private String eventId;
    private String eventType; // REVIEW_SUBMITTED
    private Long reviewId;
    private Long workerId;
    private Long customerId;
    private Integer rating;
    private String comment;
    private LocalDateTime timestamp;
}
