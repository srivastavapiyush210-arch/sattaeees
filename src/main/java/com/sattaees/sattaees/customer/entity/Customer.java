package com.sattaees.sattaees.customer.entity;

import com.sattaees.sattaees.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Customer domain entity representing users requesting services.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseAuditableEntity {

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

    @Column(nullable = false)
    private String address;
}
