package com.sattaees.sattaees.service;

import com.sattaees.sattaees.model.JobRequest;
import com.sattaees.sattaees.model.JobStatus;
import com.sattaees.sattaees.model.Customer;
import com.sattaees.sattaees.model.Worker;
import com.sattaees.sattaees.repository.JobRequestRepository;
import com.sattaees.sattaees.repository.CustomerRepository;
import com.sattaees.sattaees.repository.WorkerRepository;

import org.springframework.stereotype.Service;

import java.util.List;

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

    // CREATE JOB REQUEST
    public JobRequest createJobRequest(JobRequest jobRequest) {

        Customer customer = customerRepository
                .findById(jobRequest.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Worker worker = workerRepository
                .findById(jobRequest.getWorker().getId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        jobRequest.setCustomer(customer);
        jobRequest.setWorker(worker);
        jobRequest.setStatus(JobStatus.REQUESTED);

        return jobRequestRepository.save(jobRequest);
    }

    // GET ALL JOB REQUESTS
    public List<JobRequest> getAllJobRequests() {
        return jobRequestRepository.findAll();
    }

    // GET JOB REQUEST BY ID
    public JobRequest getJobRequestById(Long id) {
        return jobRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobRequest not found with id " + id));
    }

    // UPDATE JOB STATUS
    public JobRequest updateJobStatus(Long id, JobStatus status) {
        JobRequest jobRequest = jobRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobRequest not found with id " + id));

        jobRequest.setStatus(status);
        return jobRequestRepository.save(jobRequest);
    }

    // DELETE JOB REQUEST
    public void deleteJobRequest(Long id) {
        jobRequestRepository.deleteById(id);
    }
    public List<JobRequest> getJobsForWorker(Long workerId) {
        return jobRequestRepository.findByWorkerId(workerId);
    }
}