package com.sattaees.sattaees.worker.mapper;

import com.sattaees.sattaees.worker.dto.WorkerResponseDto;
import com.sattaees.sattaees.worker.entity.Worker;

public final class WorkerMapper {

    private WorkerMapper() {}

    public static WorkerResponseDto toResponseDto(Worker worker) {
        if (worker == null) {
            return null;
        }
        return WorkerResponseDto.builder()
                .id(worker.getId())
                .name(worker.getName())
                .email(worker.getEmail())
                .phoneNumber(worker.getPhoneNumber())
                .skill(worker.getSkill())
                .experience(worker.getExperience())
                .experienceYears(worker.getExperience())
                .city(worker.getCity())
                .available(worker.isAvailable())
                .hourlyRate(worker.getHourlyRate())
                .averageRating(worker.getAverageRating())
                .totalReviews(worker.getTotalReviews())
                .createdAt(worker.getCreatedAt())
                .build();
    }
}
