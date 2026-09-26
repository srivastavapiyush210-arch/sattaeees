package com.sattaees.sattaees.auth.controller;

import com.sattaees.sattaees.auth.dto.*;
import com.sattaees.sattaees.auth.service.AuthService;
import com.sattaees.sattaees.customer.dto.CustomerResponseDto;
import com.sattaees.sattaees.customer.service.CustomerService;
import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import com.sattaees.sattaees.worker.dto.WorkerResponseDto;
import com.sattaees.sattaees.worker.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, token refresh, and logout")
public class AuthController {

    private final AuthService authService;
    private final CustomerService customerService;
    private final WorkerService workerService;

    public AuthController(AuthService authService,
                          CustomerService customerService,
                          WorkerService workerService) {
        this.authService = authService;
        this.customerService = customerService;
        this.workerService = workerService;
    }

    @PostMapping("/register/customer")
    @Operation(summary = "Register a new customer account")
    public ResponseEntity<AuthResponse> registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        log.info("REST request to register customer: {}", request.getEmail());
        CustomerResponseDto customer = customerService.registerCustomer(request);
        AuthResponse response = authService.createAuthResponse(customer.getId(), customer.getEmail(), customer.getName(), "CUSTOMER");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register/worker")
    @Operation(summary = "Register a new worker / service expert account")
    public ResponseEntity<AuthResponse> registerWorker(@Valid @RequestBody RegisterWorkerRequest request) {
        log.info("REST request to register worker: {}", request.getEmail());
        WorkerResponseDto worker = workerService.registerWorker(request);
        AuthResponse response = authService.createAuthResponse(worker.getId(), worker.getEmail(), worker.getName(), "WORKER");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login/customer")
    @Operation(summary = "Login customer using email and password")
    public ResponseEntity<AuthResponse> loginCustomer(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to authenticate customer: {}", request.getEmail());
        return ResponseEntity.ok(authService.loginCustomer(request));
    }

    @PostMapping("/login/worker")
    @Operation(summary = "Login worker using email and password")
    public ResponseEntity<AuthResponse> loginWorker(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to authenticate worker: {}", request.getEmail());
        return ResponseEntity.ok(authService.loginWorker(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a valid refresh token for a new access token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("REST request to refresh access token");
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke active refresh tokens and logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal != null) {
            authService.logout(userPrincipal.getEmail());
        }
        return ResponseEntity.noContent().build();
    }
}
