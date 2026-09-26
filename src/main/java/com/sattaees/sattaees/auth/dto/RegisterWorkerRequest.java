package com.sattaees.sattaees.auth.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterWorkerRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone number cannot be empty")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Skill must be specified")
    @Size(max = 100, message = "Skill must not exceed 100 characters")
    private String skill;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience cannot exceed 50 years")
    private int experience;

    @NotBlank(message = "City cannot be empty")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotNull(message = "Hourly rate must be specified")
    @DecimalMin(value = "0.0", inclusive = true, message = "Hourly rate cannot be negative")
    private Double hourlyRate;
}
