package com.sattaees.sattaees.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Phone number cannot be empty")
    private String phoneNumber;

    @NotBlank(message = "Skill must be specified")
    private String skill;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience cannot exceed 50 years")
    private int experience;

    @NotBlank(message = "City cannot be empty")
    private String city;

    private boolean available = true;
    
    @NotNull(message = "Hourly rate must be specified")
    @Min(value = 0, message = "Hourly rate cannot be negative")
    private Double hourlyRate;

    @Min(value = 0, message = "Average rating cannot be negative")
    @Max(value = 5, message = "Average rating cannot exceed 5.0")
    private Double averageRating = 0.0;

    @Min(value = 0, message = "Total reviews count cannot be negative")
    private Integer totalReviews = 0;
}

