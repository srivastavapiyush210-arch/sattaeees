package com.sattaees.sattaees.job.mapper;

import com.sattaees.sattaees.job.dto.JobResponseDto;
import com.sattaees.sattaees.job.entity.JobRequest;

public final class JobMapper {

    private JobMapper() {}

    public static JobResponseDto toResponseDto(JobRequest job) {
        if (job == null) {
            return null;
        }

        JobResponseDto.CustomerSummary customerSummary = null;
        if (job.getCustomer() != null) {
            customerSummary = JobResponseDto.CustomerSummary.builder()
                    .id(job.getCustomer().getId())
                    .name(job.getCustomer().getName())
                    .email(job.getCustomer().getEmail())
                    .phoneNumber(job.getCustomer().getPhoneNumber())
                    .address(job.getCustomer().getAddress())
                    .build();
        }

        JobResponseDto.WorkerSummary workerSummary = null;
        if (job.getWorker() != null) {
            workerSummary = JobResponseDto.WorkerSummary.builder()
                    .id(job.getWorker().getId())
                    .name(job.getWorker().getName())
                    .email(job.getWorker().getEmail())
                    .phoneNumber(job.getWorker().getPhoneNumber())
                    .skill(job.getWorker().getSkill())
                    .city(job.getWorker().getCity())
                    .hourlyRate(job.getWorker().getHourlyRate())
                    .averageRating(job.getWorker().getAverageRating())
                    .build();
        }

        return JobResponseDto.builder()
                .id(job.getId())
                .serviceType(job.getServiceType())
                .location(job.getLocation())
                .status(job.getStatus())
                .customer(customerSummary)
                .worker(workerSummary)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
