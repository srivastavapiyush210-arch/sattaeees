package com.sattaees.sattaees.job.dto;

import com.sattaees.sattaees.job.entity.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDto {

    private Long id;
    private String serviceType;
    private String location;
    private JobStatus status;
    private CustomerSummary customer;
    private WorkerSummary worker;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSummary {
        private Long id;
        private String name;
        private String email;
        private String phoneNumber;
        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerSummary {
        private Long id;
        private String name;
        private String email;
        private String phoneNumber;
        private String skill;
        private String city;
        private Double hourlyRate;
        private Double averageRating;
    }
}
