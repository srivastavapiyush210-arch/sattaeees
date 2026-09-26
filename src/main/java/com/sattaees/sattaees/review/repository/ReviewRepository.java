package com.sattaees.sattaees.review.repository;

import com.sattaees.sattaees.review.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"worker", "customer"})
    @Query("SELECT r FROM Review r WHERE r.worker.id = :workerId ORDER BY r.createdAt DESC")
    List<Review> findByWorkerId(@Param("workerId") Long workerId);

    @EntityGraph(attributePaths = {"worker", "customer"})
    @Query("SELECT r FROM Review r WHERE r.customer.id = :customerId ORDER BY r.createdAt DESC")
    List<Review> findByCustomerId(@Param("customerId") Long customerId);
}
