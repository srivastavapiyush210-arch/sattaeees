package com.sattaees.sattaees.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_requests")
public class JobRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceType;

    private String location;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    public JobRequest() {
    }

    public Long getId() {
        return id;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getLocation() {
        return location;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }
}
