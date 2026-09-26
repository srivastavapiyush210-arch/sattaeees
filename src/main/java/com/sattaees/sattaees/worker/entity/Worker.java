package com.sattaees.sattaees.worker.entity;

import com.sattaees.sattaees.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * Worker domain entity representing service providers.
 * Includes optimistic concurrency locking via BaseAuditableEntity version.
 */
@Entity
@Table(name = "workers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Worker extends BaseAuditableEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String skill;

    @Column(nullable = false)
    private int experience;

    @Column(nullable = false, length = 100)
    private String city;

    @Builder.Default
    @Column(nullable = false)
    private boolean available = true;

    @Builder.Default
    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate = 0.0;

    @Builder.Default
    @Column(name = "average_rating", nullable = false)
    private Double averageRating = 0.0;

    @Builder.Default
    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews = 0;
}
