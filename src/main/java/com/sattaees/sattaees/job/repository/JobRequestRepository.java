package com.sattaees.sattaees.job.repository;

import com.sattaees.sattaees.job.entity.JobRequest;
import com.sattaees.sattaees.job.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRequestRepository extends JpaRepository<JobRequest, Long> {

    @EntityGraph(attributePaths = {"customer", "worker"})
    @Query("SELECT j FROM JobRequest j WHERE j.id = :id")
    Optional<JobRequest> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"customer", "worker"})
    @Query("SELECT j FROM JobRequest j ORDER BY j.createdAt DESC")
    List<JobRequest> findAllWithDetails();

    @EntityGraph(attributePaths = {"customer", "worker"})
    @Query("SELECT j FROM JobRequest j WHERE j.worker.id = :workerId ORDER BY j.createdAt DESC")
    List<JobRequest> findByWorkerId(@Param("workerId") Long workerId);

    @EntityGraph(attributePaths = {"customer", "worker"})
    @Query("SELECT j FROM JobRequest j WHERE j.customer.id = :customerId ORDER BY j.createdAt DESC")
    List<JobRequest> findByCustomerId(@Param("customerId") Long customerId);

    @EntityGraph(attributePaths = {"customer", "worker"})
    @Query("SELECT j FROM JobRequest j WHERE (:status IS NULL OR j.status = :status)")
    Page<JobRequest> findByStatusWithDetails(@Param("status") JobStatus status, Pageable pageable);
}
