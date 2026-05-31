package com.sattaees.sattaees.service;

import com.sattaees.sattaees.dto.CustomerDTO;
import com.sattaees.sattaees.exception.CustomerNotFoundException;
import com.sattaees.sattaees.exception.DuplicateResourceException;
import com.sattaees.sattaees.model.Customer;
import com.sattaees.sattaees.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer createCustomer(Customer customer) {
        log.info("Attempting to register customer with email: {}", customer.getEmail());
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            log.warn("Customer registration failed: Email {} is already taken", customer.getEmail());
            throw new DuplicateResourceException("Email address already registered: " + customer.getEmail());
        }
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully with ID: {}", savedCustomer.getId());
        return savedCustomer;
    }
    
    public Customer loginCustomer(String email, String password) {
        log.info("Processing login request for customer: {}", email);
        return customerRepository.findByEmail(email)
            .filter(c -> {
                boolean match = passwordEncoder.matches(password, c.getPassword());
                if (match) {
                    log.info("Login successful for customer: {}", email);
                } else {
                    log.warn("Login failed for customer: {} (password mismatch)", email);
                }
                return match;
            })
            .orElseGet(() -> {
                log.warn("Login failed: Customer with email {} not found", email);
                return null;
            });
    }

    public List<CustomerDTO> getAllCustomers() {
        log.debug("Fetching all customers");
        return customerRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public CustomerDTO getCustomerById(Long id) {
        log.debug("Fetching customer details by ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer lookup failed: ID {} not found", id);
                    return new CustomerNotFoundException("Customer not found with id " + id);
                });

        return mapToDTO(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        log.info("Updating customer details for ID: {}", id);
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer update failed: ID {} not found", id);
                    return new CustomerNotFoundException("Customer not found with id " + id);
                });

        existingCustomer.setName(updatedCustomer.getName());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setPassword(updatedCustomer.getPassword());
        existingCustomer.setPhoneNumber(updatedCustomer.getPhoneNumber());
        existingCustomer.setAddress(updatedCustomer.getAddress());

        Customer savedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer details updated successfully for ID: {}", id);
        return savedCustomer;
    }

    public void deleteCustomer(Long id) {
        log.info("Request to delete customer ID: {}", id);
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer deletion failed: ID {} not found", id);
                    return new CustomerNotFoundException("Customer not found with id " + id);
                });

        customerRepository.delete(existingCustomer);
        log.info("Customer ID: {} deleted successfully", id);
    }

    private CustomerDTO mapToDTO(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getAddress()
        );
    }
}

