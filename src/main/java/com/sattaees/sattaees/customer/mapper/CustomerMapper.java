package com.sattaees.sattaees.customer.mapper;

import com.sattaees.sattaees.customer.dto.CustomerResponseDto;
import com.sattaees.sattaees.customer.entity.Customer;

public final class CustomerMapper {

    private CustomerMapper() {}

    public static CustomerResponseDto toResponseDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        return CustomerResponseDto.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
