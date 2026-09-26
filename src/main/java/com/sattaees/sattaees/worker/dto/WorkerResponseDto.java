package com.sattaees.sattaees.worker.dto;

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
public class WorkerResponseDto implements Serializable {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String skill;
    private int experience;
    private int experienceYears;
    private String city;
    private boolean available;
    private Double hourlyRate;
    private Double averageRating;
    private Integer totalReviews;
    private LocalDateTime createdAt;
}
