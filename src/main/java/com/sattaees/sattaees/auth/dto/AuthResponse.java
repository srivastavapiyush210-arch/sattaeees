package com.sattaees.sattaees.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // Primary token alias for frontend compatibility
    private String token;

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;

    private UserSummaryDto user;
}
