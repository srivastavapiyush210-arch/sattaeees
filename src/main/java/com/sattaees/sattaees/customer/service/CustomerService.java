package com.sattaees.sattaees.customer.service;

import com.sattaees.sattaees.auth.dto.RegisterCustomerRequest;
import com.sattaees.sattaees.common.exception.DuplicateResourceException;
import com.sattaees.sattaees.common.exception.ResourceNotFoundException;
import com.sattaees.sattaees.common.exception.UnauthorizedException;
import com.sattaees.sattaees.customer.dto.CustomerResponseDto;
import com.sattaees.sattaees.customer.dto.CustomerUpdateDto;
import com.sattaees.sattaees.customer.entity.Customer;
import com.sattaees.sattaees.customer.mapper.CustomerMapper;
import com.sattaees.sattaees.customer.repository.CustomerRepository;
import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling customer profile management, registration, and persistence.
 */
@Slf4j
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerResponseDto registerCustomer(RegisterCustomerRequest request) {
        log.info("Registering customer with email: {}", request.getEmail());
        if (customerRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration conflict: Customer email {} already exists", request.getEmail());
            throw new DuplicateResourceException("Customer email already registered: " + request.getEmail());
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Successfully registered customer ID: {}", saved.getId());
        return CustomerMapper.toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(Long id) {
        log.debug("Fetching customer by ID: {}", id);
        Customer customer = findCustomerEntityById(id);
        return CustomerMapper.toResponseDto(customer);
    }

    @Transactional(readOnly = true)
    public Customer findCustomerEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getAllCustomers() {
        log.debug("Fetching all customers");
        return customerRepository.findAll().stream()
                .map(CustomerMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public CustomerResponseDto updateCustomer(Long id, CustomerUpdateDto updateDto, UserPrincipal currentUser) {
        log.info("Updating customer profile for ID: {}", id);
        verifyCustomerOwnership(id, currentUser);

        Customer customer = findCustomerEntityById(id);
        customer.setName(updateDto.getName());
        customer.setPhoneNumber(updateDto.getPhoneNumber());
        customer.setAddress(updateDto.getAddress());

        Customer updated = customerRepository.save(customer);
        log.info("Updated customer profile for ID: {}", id);
        return CustomerMapper.toResponseDto(updated);
    }

    @Transactional
    public void deleteCustomer(Long id, UserPrincipal currentUser) {
        log.info("Request to delete customer ID: {}", id);
        verifyCustomerOwnership(id, currentUser);

        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
        log.info("Deleted customer ID: {}", id);
    }

    private void verifyCustomerOwnership(Long id, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required.");
        }
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = currentUser.getId().equals(id) && "CUSTOMER".equalsIgnoreCase(currentUser.getRole());

        if (!isAdmin && !isOwner) {
            log.warn("Access denied: User {} tried modifying customer {}", currentUser.getEmail(), id);
            throw new UnauthorizedException("You are not authorized to modify this customer profile.");
        }
    }
}
