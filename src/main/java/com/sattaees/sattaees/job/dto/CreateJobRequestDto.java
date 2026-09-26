package com.sattaees.sattaees.job.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequestDto {

    @NotBlank(message = "Service type cannot be empty")
    private String serviceType;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    private Long customerId;
    private Long workerId;

    // Supports frontend nested object format: { customer: { id: 1 }, worker: { id: 2 } }
    @JsonSetter("customer")
    public void setCustomerObject(Map<String, Object> customer) {
        if (customer != null && customer.get("id") != null) {
            this.customerId = Long.valueOf(customer.get("id").toString());
        }
    }

    @JsonSetter("worker")
    public void setWorkerObject(Map<String, Object> worker) {
        if (worker != null && worker.get("id") != null) {
            this.workerId = Long.valueOf(worker.get("id").toString());
        }
    }
}
