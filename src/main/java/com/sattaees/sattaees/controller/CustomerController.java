package com.sattaees.sattaees.controller;

import com.sattaees.sattaees.dto.AuthResponse;
import com.sattaees.sattaees.dto.CustomerDTO;
import com.sattaees.sattaees.dto.LoginRequest;
import com.sattaees.sattaees.model.Customer;
import com.sattaees.sattaees.service.CustomerService;
import com.sattaees.sattaees.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final JwtUtil jwtUtil;

    public CustomerController(CustomerService customerService, JwtUtil jwtUtil) {
        this.customerService = customerService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody Customer customer) {
        log.info("REST request to register a new customer: {}", customer.getEmail());
        Customer savedCustomer = customerService.createCustomer(customer);
        String token = jwtUtil.generateToken(savedCustomer.getEmail(), "CUSTOMER", savedCustomer.getId());
        return new ResponseEntity<>(new AuthResponse(token, savedCustomer), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        log.info("REST request to login customer: {}", req.getEmail());
        Customer c = customerService.loginCustomer(req.getEmail(), req.getPassword());
        if (c != null) {
            String token = jwtUtil.generateToken(c.getEmail(), "CUSTOMER", c.getId());
            return ResponseEntity.ok(new AuthResponse(token, c));
        }
        log.warn("Customer login failed for: {}", req.getEmail());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        log.info("REST request to get all customers");
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        log.info("REST request to get customer details for ID: {}", id);
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody Customer customer) {
        log.info("REST request to update customer ID: {}", id);
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("REST request to delete customer ID: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}

