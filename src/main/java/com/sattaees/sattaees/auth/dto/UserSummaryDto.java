package com.sattaees.sattaees.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String name;
    private String email;
    private String role;
}
