package com.sattaees.sattaees.customer.controller;

import com.sattaees.sattaees.auth.dto.AuthResponse;
import com.sattaees.sattaees.auth.dto.LoginRequest;
import com.sattaees.sattaees.auth.dto.RegisterCustomerRequest;
import com.sattaees.sattaees.auth.service.AuthService;
import com.sattaees.sattaees.customer.dto.CustomerResponseDto;
import com.sattaees.sattaees.customer.dto.CustomerUpdateDto;
import com.sattaees.sattaees.customer.service.CustomerService;
import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "Endpoints for customer operations, profiles, and management")
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;

    public CustomerController(CustomerService customerService, AuthService authService) {
        this.customerService = customerService;
        this.authService = authService;
    }

    @PostMapping
    @Operation(summary = "Register customer and return auth token (Compatible with frontend)")
    public ResponseEntity<AuthResponse> createCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        log.info("REST request to register customer: {}", request.getEmail());
        CustomerResponseDto customer = customerService.registerCustomer(request);
        AuthResponse response = authService.createAuthResponse(customer.getId(), customer.getEmail(), customer.getName(), "CUSTOMER");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Customer login (Compatible with frontend)")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login customer: {}", request.getEmail());
        return ResponseEntity.ok(authService.loginCustomer(request));
    }

    @GetMapping
    @Operation(summary = "Get list of all customers")
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        log.info("REST request to get all customers");
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer profile by ID")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long id) {
        log.info("REST request to get customer ID: {}", id);
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer profile")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateDto updateDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST request to update customer ID: {}", id);
        return ResponseEntity.ok(customerService.updateCustomer(id, updateDto, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer account")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST request to delete customer ID: {}", id);
        customerService.deleteCustomer(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
