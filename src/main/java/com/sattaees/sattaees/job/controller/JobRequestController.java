package com.sattaees.sattaees.job.controller;

import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import com.sattaees.sattaees.job.dto.CreateJobRequestDto;
import com.sattaees.sattaees.job.dto.JobResponseDto;
import com.sattaees.sattaees.job.dto.UpdateJobStatusDto;
import com.sattaees.sattaees.job.entity.JobStatus;
import com.sattaees.sattaees.job.service.JobRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/job-requests")
@Tag(name = "Job Requests", description = "Endpoints for booking jobs, lifecycle state transitions, and tracking orders")
public class JobRequestController {

    private final JobRequestService jobRequestService;

    public JobRequestController(JobRequestService jobRequestService) {
        this.jobRequestService = jobRequestService;
    }

    @PostMapping
    @Operation(summary = "Create a new job request / booking")
    public ResponseEntity<JobResponseDto> createJobRequest(
            @Valid @RequestBody CreateJobRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST request to book job: service={}, worker={}", requestDto.getServiceType(), requestDto.getWorkerId());
        JobResponseDto response = jobRequestService.createJobRequest(requestDto, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all job requests")
    public ResponseEntity<List<JobResponseDto>> getAllJobRequests() {
        log.info("REST request to fetch all job requests");
        return ResponseEntity.ok(jobRequestService.getAllJobRequests());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job request details by ID")
    public ResponseEntity<JobResponseDto> getJobRequestById(@PathVariable Long id) {
        log.info("REST request to fetch job request ID: {}", id);
        return ResponseEntity.ok(jobRequestService.getJobRequestById(id));
    }

    @GetMapping("/worker/{workerId}")
    @Operation(summary = "Get all jobs booked with a specific worker")
    public ResponseEntity<List<JobResponseDto>> getJobsForWorker(@PathVariable Long workerId) {
        log.info("REST request to fetch jobs for worker ID: {}", workerId);
        return ResponseEntity.ok(jobRequestService.getJobsForWorker(workerId));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all jobs booked by a specific customer")
    public ResponseEntity<List<JobResponseDto>> getJobsForCustomer(@PathVariable Long customerId) {
        log.info("REST request to fetch jobs for customer ID: {}", customerId);
        return ResponseEntity.ok(jobRequestService.getJobsForCustomer(customerId));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update job status with state machine transition checks")
    public ResponseEntity<JobResponseDto> updateJobStatus(
            @PathVariable Long id,
            @RequestParam(required = false) JobStatus status,
            @RequestBody(required = false) UpdateJobStatusDto bodyDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        JobStatus targetStatus = (status != null) ? status : (bodyDto != null ? bodyDto.getStatus() : null);
        if (targetStatus == null) {
            return ResponseEntity.badRequest().build();
        }
        log.info("REST request to update job ID: {} to status: {}", id, targetStatus);
        return ResponseEntity.ok(jobRequestService.updateJobStatus(id, targetStatus, currentUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job request")
    public ResponseEntity<Void> deleteJobRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST request to delete job request ID: {}", id);
        jobRequestService.deleteJobRequest(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
