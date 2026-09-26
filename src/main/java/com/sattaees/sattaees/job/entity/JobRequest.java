package com.sattaees.sattaees.job.entity;

import com.sattaees.sattaees.common.entity.BaseAuditableEntity;
import com.sattaees.sattaees.customer.entity.Customer;
import com.sattaees.sattaees.worker.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

/**
 * JobRequest entity representing a service booking lifecycle.
 * Uses lazy loading and BaseAuditableEntity versioning for optimistic locking.
 */
@Entity
@Table(name = "job_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_type", nullable = false, length = 100)
    private String serviceType;

    @Column(nullable = false, length = 255)
    private String location;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobStatus status = JobStatus.REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;
}
