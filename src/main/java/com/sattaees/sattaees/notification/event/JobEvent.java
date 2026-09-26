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
public class JobEvent implements Serializable {

    private String eventId;
    private String eventType; // JOB_CREATED, JOB_ACCEPTED, JOB_IN_PROGRESS, JOB_COMPLETED, JOB_CANCELLED
    private Long jobId;
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private Long workerId;
    private String workerEmail;
    private String workerName;
    private String serviceType;
    private String status;
    private LocalDateTime timestamp;
}
