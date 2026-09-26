package com.sattaees.sattaees.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCustomerRequest {

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

    @NotBlank(message = "Address cannot be empty")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
}
