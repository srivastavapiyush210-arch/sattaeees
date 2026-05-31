package com.sattaees.sattaees.repository;

import com.sattaees.sattaees.model.JobRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRequestRepository extends JpaRepository<JobRequest, Long> {

    // Find all jobs assigned to a specific worker
    List<JobRequest> findByWorkerId(Long workerId);

}
