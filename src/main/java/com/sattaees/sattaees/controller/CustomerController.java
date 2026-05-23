package com.sattaees.sattaees.controller;

import com.sattaees.sattaees.dto.CustomerDTO;
import com.sattaees.sattaees.model.Customer;
import com.sattaees.sattaees.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final com.sattaees.sattaees.util.JwtUtil jwtUtil;

    public CustomerController(CustomerService customerService, com.sattaees.sattaees.util.JwtUtil jwtUtil) {
        this.customerService = customerService;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerService.createCustomer(customer);
        String token = jwtUtil.generateToken(savedCustomer.getEmail(), "CUSTOMER", savedCustomer.getId());
        return new ResponseEntity<>(new com.sattaees.sattaees.dto.AuthResponse(token, savedCustomer), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody com.sattaees.sattaees.dto.LoginRequest req) {
        Customer c = customerService.loginCustomer(req.getEmail(), req.getPassword());
        if(c != null) {
            String token = jwtUtil.generateToken(c.getEmail(), "CUSTOMER", c.getId());
            return ResponseEntity.ok(new com.sattaees.sattaees.dto.AuthResponse(token, c));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }


    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }


    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer customer) {

        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
