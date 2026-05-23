package com.sattaees.sattaees.service;

import com.sattaees.sattaees.dto.CustomerDTO;
import com.sattaees.sattaees.exception.CustomerNotFoundException;
import com.sattaees.sattaees.model.Customer;
import com.sattaees.sattaees.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public Customer createCustomer(Customer customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }
    
    public Customer loginCustomer(String email, String password) {
        return customerRepository.findByEmail(email)
            .filter(c -> passwordEncoder.matches(password, c.getPassword()))
            .orElse(null);
    }


    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }


    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer not found with id " + id)
                );

        return mapToDTO(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer not found with id " + id)
                );

        existingCustomer.setName(updatedCustomer.getName());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setPassword(updatedCustomer.getPassword());
        existingCustomer.setPhoneNumber(updatedCustomer.getPhoneNumber());
        existingCustomer.setAddress(updatedCustomer.getAddress());

        return customerRepository.save(existingCustomer);
    }


    public void deleteCustomer(Long id) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer not found with id " + id)
                );

        customerRepository.delete(existingCustomer);
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
