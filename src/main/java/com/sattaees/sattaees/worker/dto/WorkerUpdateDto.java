package com.sattaees.sattaees.worker.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerUpdateDto {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Phone number cannot be empty")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Skill must be specified")
    @Size(max = 100, message = "Skill must not exceed 100 characters")
    private String skill;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience cannot exceed 50 years")
    private Integer experience;

    @NotBlank(message = "City cannot be empty")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    private Boolean available;

    @DecimalMin(value = "0.0", inclusive = true, message = "Hourly rate cannot be negative")
    private Double hourlyRate;
}
