package com.sattaees.sattaees.auth.service;

import com.sattaees.sattaees.auth.entity.RefreshToken;
import com.sattaees.sattaees.auth.repository.RefreshTokenRepository;
import com.sattaees.sattaees.common.exception.UnauthorizedException;
import com.sattaees.sattaees.infrastructure.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                                JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId, String email, String role) {
        // Revoke any existing active refresh tokens for single active session per role/user
        refreshTokenRepository.revokeAllByUserEmail(email);

        String rawToken = jwtTokenProvider.generateRefreshToken(userId, email, role);
        long expirationMillis = jwtTokenProvider.getRefreshTokenExpirationMillis();
        LocalDateTime expiryDate = LocalDateTime.now().plus(expirationMillis, ChronoUnit.MILLIS);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(rawToken)
                .userEmail(email)
                .userRole(role.toUpperCase())
                .expiryDate(expiryDate)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Issued new refresh token for user: {}", email);
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            log.warn("Attempt to use revoked refresh token for user: {}", token.getUserEmail());
            throw new UnauthorizedException("Refresh token has been revoked. Please log in again.");
        }
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Expired refresh token for user: {}", token.getUserEmail());
            throw new UnauthorizedException("Refresh token has expired. Please log in again.");
        }
        return token;
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("Revoked refresh token for user: {}", rt.getUserEmail());
        });
    }

    @Transactional
    public void revokeAllForUser(String email) {
        refreshTokenRepository.revokeAllByUserEmail(email);
        log.info("Revoked all active refresh tokens for user: {}", email);
    }
}
