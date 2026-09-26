package com.sattaees.sattaees.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_role", nullable = false)
    private String userRole;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean revoked = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
