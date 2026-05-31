package com.sattaees.sattaees.service;

import com.sattaees.sattaees.exception.InvalidJobStatusException;
import com.sattaees.sattaees.exception.ResourceNotFoundException;
import com.sattaees.sattaees.model.JobRequest;
import com.sattaees.sattaees.model.JobStatus;
import com.sattaees.sattaees.model.Customer;
import com.sattaees.sattaees.model.Worker;
import com.sattaees.sattaees.repository.JobRequestRepository;
import com.sattaees.sattaees.repository.CustomerRepository;
import com.sattaees.sattaees.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class JobRequestService {

    private final JobRequestRepository jobRequestRepository;
    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;

    public JobRequestService(JobRequestRepository jobRequestRepository,
                             CustomerRepository customerRepository,
                             WorkerRepository workerRepository) {
        this.jobRequestRepository = jobRequestRepository;
        this.customerRepository = customerRepository;
        this.workerRepository = workerRepository;
    }

    public JobRequest createJobRequest(JobRequest jobRequest) {
        log.info("Booking new job request for customer ID: {} with worker ID: {}", 
                jobRequest.getCustomer().getId(), jobRequest.getWorker().getId());

        Customer customer = customerRepository
                .findById(jobRequest.getCustomer().getId())
                .orElseThrow(() -> {
                    log.warn("Job booking failed: Customer ID {} not found", jobRequest.getCustomer().getId());
                    return new ResourceNotFoundException("Customer not found");
                });

        Worker worker = workerRepository
                .findById(jobRequest.getWorker().getId())
                .orElseThrow(() -> {
                    log.warn("Job booking failed: Worker ID {} not found", jobRequest.getWorker().getId());
                    return new ResourceNotFoundException("Worker not found");
                });

        jobRequest.setCustomer(customer);
        jobRequest.setWorker(worker);
        jobRequest.setStatus(JobStatus.REQUESTED);

        JobRequest savedRequest = jobRequestRepository.save(jobRequest);
        log.info("Job request successfully booked with ID: {}", savedRequest.getId());
        return savedRequest;
    }

    public List<JobRequest> getAllJobRequests() {
        log.debug("Fetching all job requests");
        return jobRequestRepository.findAll();
    }

    public JobRequest getJobRequestById(Long id) {
        log.debug("Fetching job request by ID: {}", id);
        return jobRequestRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Job request lookup failed: ID {} not found", id);
                    return new ResourceNotFoundException("JobRequest not found with id " + id);
                });
    }

    public JobRequest updateJobStatus(Long id, JobStatus status) {
        log.info("Request to update job status for ID: {} to {}", id, status);
        JobRequest jobRequest = jobRequestRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Job status update failed: ID {} not found", id);
                    return new ResourceNotFoundException("JobRequest not found with id " + id);
                });

        JobStatus currentStatus = jobRequest.getStatus();
        
        // Basic workflow transition logic
        if (currentStatus == JobStatus.COMPLETED) {
            log.warn("Invalid status change: Job {} is already COMPLETED", id);
            throw new InvalidJobStatusException("Cannot modify a completed job request.");
        }
        
        if (currentStatus == JobStatus.REQUESTED && status == JobStatus.COMPLETED) {
            log.warn("Invalid status change: Cannot transition job {} directly from REQUESTED to COMPLETED", id);
            throw new InvalidJobStatusException("Job must be ACCEPTED before it can be marked as COMPLETED.");
        }

        jobRequest.setStatus(status);
        JobRequest savedRequest = jobRequestRepository.save(jobRequest);
        log.info("Job ID: {} status updated from {} to {}", id, currentStatus, status);
        return savedRequest;
    }

    public void deleteJobRequest(Long id) {
        log.info("Request to delete job request ID: {}", id);
        if (!jobRequestRepository.existsById(id)) {
            log.warn("Job request deletion failed: ID {} not found", id);
            throw new ResourceNotFoundException("JobRequest not found with id " + id);
        }
        jobRequestRepository.deleteById(id);
        log.info("Job request ID: {} deleted successfully", id);
    }

    public List<JobRequest> getJobsForWorker(Long workerId) {
        log.debug("Fetching all jobs for worker ID: {}", workerId);
        return jobRequestRepository.findByWorkerId(workerId);
    }
}