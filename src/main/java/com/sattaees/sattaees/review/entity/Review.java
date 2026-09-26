package com.sattaees.sattaees.review.entity;

import com.sattaees.sattaees.common.entity.BaseAuditableEntity;
import com.sattaees.sattaees.customer.entity.Customer;
import com.sattaees.sattaees.worker.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

/**
 * Review domain entity representing customer feedback and ratings for workers.
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
