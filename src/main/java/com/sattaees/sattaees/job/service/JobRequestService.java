package com.sattaees.sattaees.job.service;

import com.sattaees.sattaees.common.exception.InvalidOperationException;
import com.sattaees.sattaees.common.exception.ResourceNotFoundException;
import com.sattaees.sattaees.common.exception.UnauthorizedException;
import com.sattaees.sattaees.customer.entity.Customer;
import com.sattaees.sattaees.customer.repository.CustomerRepository;
import com.sattaees.sattaees.infrastructure.config.KafkaConfig;
import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import com.sattaees.sattaees.job.dto.CreateJobRequestDto;
import com.sattaees.sattaees.job.dto.JobResponseDto;
import com.sattaees.sattaees.job.entity.JobRequest;
import com.sattaees.sattaees.job.entity.JobStatus;
import com.sattaees.sattaees.job.mapper.JobMapper;
import com.sattaees.sattaees.job.repository.JobRequestRepository;
import com.sattaees.sattaees.notification.event.JobEvent;
import com.sattaees.sattaees.worker.entity.Worker;
import com.sattaees.sattaees.worker.repository.WorkerRepository;
import com.sattaees.sattaees.worker.service.WorkerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service managing job request lifecycles, state transitions,
 * optimistic concurrency locking, and asynchronous Kafka event streaming.
 */
@Slf4j
@Service
public class JobRequestService {

    private final JobRequestRepository jobRequestRepository;
    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;
    private final WorkerService workerService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public JobRequestService(JobRequestRepository jobRequestRepository,
                             CustomerRepository customerRepository,
                             WorkerRepository workerRepository,
                             WorkerService workerService,
                             KafkaTemplate<String, Object> kafkaTemplate) {
        this.jobRequestRepository = jobRequestRepository;
        this.customerRepository = customerRepository;
        this.workerRepository = workerRepository;
        this.workerService = workerService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public JobResponseDto createJobRequest(CreateJobRequestDto dto, UserPrincipal currentUser) {
        Long customerId = dto.getCustomerId();
        if (customerId == null && currentUser != null && "CUSTOMER".equalsIgnoreCase(currentUser.getRole())) {
            customerId = currentUser.getId();
        }

        if (customerId == null) {
            throw new InvalidOperationException("Customer ID is required to create a job request.");
        }
        if (dto.getWorkerId() == null) {
            throw new InvalidOperationException("Worker ID is required to book a service.");
        }

        log.info("Booking job for customer ID: {} with worker ID: {}", customerId, dto.getWorkerId());

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));

        Worker worker = workerRepository.findById(dto.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with id: " + dto.getWorkerId()));

        if (!worker.isAvailable()) {
            log.warn("Worker ID: {} is currently busy or unavailable", worker.getId());
            throw new InvalidOperationException("Worker is currently unavailable for new bookings.");
        }

        JobRequest jobRequest = JobRequest.builder()
                .serviceType(dto.getServiceType())
                .location(dto.getLocation())
                .status(JobStatus.REQUESTED)
                .customer(customer)
                .worker(worker)
                .build();

        JobRequest saved = jobRequestRepository.save(jobRequest);
        log.info("Job request created with ID: {}", saved.getId());

        // Publish asynchronous Kafka event
        publishJobEvent("JOB_CREATED", saved);

        return JobMapper.toResponseDto(saved);
    }

    @Transactional
    public JobResponseDto updateJobStatus(Long id, JobStatus newStatus, UserPrincipal currentUser) {
        log.info("Request to transition job ID: {} to status: {}", id, newStatus);
        JobRequest job = jobRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobRequest not found with id: " + id));

        JobStatus currentStatus = job.getStatus();

        // Validate state machine transitions
        validateStatusTransition(currentStatus, newStatus);

        job.setStatus(newStatus);
        JobRequest updated = jobRequestRepository.save(job);

        // Adjust worker availability based on lifecycle events
        if (newStatus == JobStatus.ACCEPTED) {
            workerService.setAvailability(job.getWorker().getId(), false);
        } else if (newStatus == JobStatus.COMPLETED || newStatus == JobStatus.CANCELLED) {
            workerService.setAvailability(job.getWorker().getId(), true);
        }

        log.info("Job ID: {} successfully transitioned from {} to {}", id, currentStatus, newStatus);

        // Publish asynchronous Kafka event
        publishJobEvent("JOB_" + newStatus.name(), updated);

        return JobMapper.toResponseDto(updated);
    }

    @Transactional(readOnly = true)
    public List<JobResponseDto> getAllJobRequests() {
        log.debug("Fetching all job requests with details (N+1 prevented)");
        return jobRequestRepository.findAllWithDetails().stream()
                .map(JobMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobResponseDto getJobRequestById(Long id) {
        log.debug("Fetching job request by ID: {}", id);
        JobRequest job = jobRequestRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobRequest not found with id: " + id));
        return JobMapper.toResponseDto(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponseDto> getJobsForWorker(Long workerId) {
        log.debug("Fetching jobs for worker ID: {} (N+1 prevented)", workerId);
        return jobRequestRepository.findByWorkerId(workerId).stream()
                .map(JobMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponseDto> getJobsForCustomer(Long customerId) {
        log.debug("Fetching jobs for customer ID: {} (N+1 prevented)", customerId);
        return jobRequestRepository.findByCustomerId(customerId).stream()
                .map(JobMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void deleteJobRequest(Long id, UserPrincipal currentUser) {
        log.info("Deleting job request ID: {}", id);
        if (!jobRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("JobRequest not found with id: " + id);
        }
        jobRequestRepository.deleteById(id);
        log.info("Deleted job request ID: {}", id);
    }

    private void validateStatusTransition(JobStatus current, JobStatus target) {
        if (current == target) {
            return;
        }

        if (current == JobStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot modify a completed job request.");
        }
        if (current == JobStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot modify a cancelled job request.");
        }

        switch (current) {
            case REQUESTED -> {
                if (target != JobStatus.ACCEPTED && target != JobStatus.CANCELLED) {
                    throw new InvalidOperationException("Job in REQUESTED status can only transition to ACCEPTED or CANCELLED.");
                }
            }
            case ACCEPTED -> {
                if (target != JobStatus.IN_PROGRESS && target != JobStatus.CANCELLED) {
                    throw new InvalidOperationException("Job in ACCEPTED status can only transition to IN_PROGRESS or CANCELLED.");
                }
            }
            case IN_PROGRESS -> {
                if (target != JobStatus.COMPLETED && target != JobStatus.CANCELLED) {
                    throw new InvalidOperationException("Job in IN_PROGRESS status can only transition to COMPLETED or CANCELLED.");
                }
            }
        }
    }

    private void publishJobEvent(String eventType, JobRequest job) {
        try {
            JobEvent event = JobEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .jobId(job.getId())
                    .customerId(job.getCustomer().getId())
                    .customerEmail(job.getCustomer().getEmail())
                    .customerName(job.getCustomer().getName())
                    .workerId(job.getWorker().getId())
                    .workerEmail(job.getWorker().getEmail())
                    .workerName(job.getWorker().getName())
                    .serviceType(job.getServiceType())
                    .status(job.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(KafkaConfig.TOPIC_JOB_EVENTS, String.valueOf(job.getId()), event);
            log.info("Dispatched Kafka JobEvent '{}' for job ID: {}", eventType, job.getId());
        } catch (Exception ex) {
            log.warn("Failed to publish Kafka event for job ID: {}. Non-fatal warning: {}", job.getId(), ex.getMessage());
        }
    }
}
