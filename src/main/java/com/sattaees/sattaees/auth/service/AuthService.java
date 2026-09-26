package com.sattaees.sattaees.auth.service;

import com.sattaees.sattaees.auth.dto.*;
import com.sattaees.sattaees.auth.entity.RefreshToken;
import com.sattaees.sattaees.common.exception.UnauthorizedException;
import com.sattaees.sattaees.customer.entity.Customer;
import com.sattaees.sattaees.customer.repository.CustomerRepository;
import com.sattaees.sattaees.infrastructure.security.JwtTokenProvider;
import com.sattaees.sattaees.worker.entity.Worker;
import com.sattaees.sattaees.worker.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthService(CustomerRepository customerRepository,
                       WorkerRepository workerRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenService refreshTokenService) {
        this.customerRepository = customerRepository;
        this.workerRepository = workerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse loginCustomer(LoginRequest req) {
        log.info("Processing customer login attempt for email: {}", req.getEmail());
        Customer customer = customerRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), customer.getPassword())) {
            log.warn("Password mismatch for customer email: {}", req.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        return createAuthResponse(customer.getId(), customer.getEmail(), customer.getName(), "CUSTOMER");
    }

    @Transactional
    public AuthResponse loginWorker(LoginRequest req) {
        log.info("Processing worker login attempt for email: {}", req.getEmail());
        Worker worker = workerRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), worker.getPassword())) {
            log.warn("Password mismatch for worker email: {}", req.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        return createAuthResponse(worker.getId(), worker.getEmail(), worker.getName(), "WORKER");
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest req) {
        RefreshToken token = refreshTokenService.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        refreshTokenService.verifyExpiration(token);

        Long userId = jwtTokenProvider.extractUserId(token.getToken());
        String email = token.getUserEmail();
        String role = token.getUserRole();

        String name = "";
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            name = customerRepository.findByEmail(email).map(Customer::getName).orElse("Customer");
        } else if ("WORKER".equalsIgnoreCase(role)) {
            name = workerRepository.findByEmail(email).map(Worker::getName).orElse("Worker");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email, role);
        long expiresIn = jwtTokenProvider.getAccessTokenExpirationMillis() / 1000;

        return AuthResponse.builder()
                .token(newAccessToken)
                .accessToken(newAccessToken)
                .refreshToken(token.getToken())
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(UserSummaryDto.builder()
                        .id(userId)
                        .name(name)
                        .email(email)
                        .role(role)
                        .build())
                .build();
    }

    @Transactional
    public void logout(String email) {
        refreshTokenService.revokeAllForUser(email);
        log.info("Logged out user: {}", email);
    }

    public AuthResponse createAuthResponse(Long userId, String email, String name, String role) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, email, role);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId, email, role);
        long expiresIn = jwtTokenProvider.getAccessTokenExpirationMillis() / 1000;

        return AuthResponse.builder()
                .token(accessToken)
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(UserSummaryDto.builder()
                        .id(userId)
                        .name(name)
                        .email(email)
                        .role(role.toUpperCase())
                        .build())
                .build();
    }
}
