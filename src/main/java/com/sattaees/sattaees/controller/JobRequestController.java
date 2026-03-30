package com.sattaees.sattaees.controller;

import com.sattaees.sattaees.model.JobRequest;
import com.sattaees.sattaees.model.JobStatus;
import com.sattaees.sattaees.service.JobRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/job-requests")
public class JobRequestController {

    private final JobRequestService jobRequestService;

    public JobRequestController(JobRequestService jobRequestService) {
        this.jobRequestService = jobRequestService;
    }

    // CREATE JOB REQUEST
    @PostMapping
    public ResponseEntity<JobRequest> createJobRequest(@RequestBody JobRequest jobRequest) {
        return ResponseEntity.ok(jobRequestService.createJobRequest(jobRequest));
    }

    // GET ALL JOB REQUESTS
    @GetMapping
    public ResponseEntity<List<JobRequest>> getAllJobRequests() {
        return ResponseEntity.ok(jobRequestService.getAllJobRequests());
    }

    // GET JOB REQUEST BY ID
    @GetMapping("/{id}")
    public ResponseEntity<JobRequest> getJobRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(jobRequestService.getJobRequestById(id));
    }
    // GET JOBS FOR SPECIFIC WORKER
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<JobRequest>> getJobsForWorker(@PathVariable Long workerId) {
        return ResponseEntity.ok(jobRequestService.getJobsForWorker(workerId));
    }

    // UPDATE JOB STATUS
    @PutMapping("/{id}/status")
    public ResponseEntity<JobRequest> updateJobStatus(
            @PathVariable Long id,
            @RequestParam JobStatus status) {

        return ResponseEntity.ok(jobRequestService.updateJobStatus(id, status));
    }

    // DELETE JOB REQUEST
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobRequest(@PathVariable Long id) {
        jobRequestService.deleteJobRequest(id);
        return ResponseEntity.noContent().build();
    }
}