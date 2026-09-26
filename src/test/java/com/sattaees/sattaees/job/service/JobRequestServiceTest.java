package com.sattaees.sattaees.job.service;

import com.sattaees.sattaees.common.exception.InvalidOperationException;
import com.sattaees.sattaees.customer.entity.Customer;
import com.sattaees.sattaees.customer.repository.CustomerRepository;
import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import com.sattaees.sattaees.job.dto.CreateJobRequestDto;
import com.sattaees.sattaees.job.dto.JobResponseDto;
import com.sattaees.sattaees.job.entity.JobRequest;
import com.sattaees.sattaees.job.entity.JobStatus;
import com.sattaees.sattaees.job.repository.JobRequestRepository;
import com.sattaees.sattaees.worker.entity.Worker;
import com.sattaees.sattaees.worker.repository.WorkerRepository;
import com.sattaees.sattaees.worker.service.WorkerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobRequestServiceTest {

    @Mock
    private JobRequestRepository jobRequestRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private WorkerService workerService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private JobRequestService jobRequestService;

    private Customer sampleCustomer;
    private Worker sampleWorker;
    private JobRequest sampleJob;

    @BeforeEach
    void setUp() {
        sampleCustomer = Customer.builder()
                .id(1L)
                .name("Aman")
                .email("aman@test.com")
                .phoneNumber("+91 9999999999")
                .address("Connaught Place, Delhi")
                .build();

        sampleWorker = Worker.builder()
                .id(2L)
                .name("Sunil")
                .email("sunil@test.com")
                .phoneNumber("+91 8888888888")
                .skill("Carpenter")
                .available(true)
                .hourlyRate(35.0)
                .build();

        sampleJob = JobRequest.builder()
                .id(10L)
                .serviceType("Carpenter")
                .location("Delhi")
                .status(JobStatus.REQUESTED)
                .customer(sampleCustomer)
                .worker(sampleWorker)
                .build();
    }

    @Test
    @DisplayName("Should create job request successfully when worker is available")
    void createJobRequest_Success() {
        CreateJobRequestDto dto = CreateJobRequestDto.builder()
                .customerId(1L)
                .workerId(2L)
                .serviceType("Carpenter")
                .location("Delhi")
                .build();

        UserPrincipal principal = UserPrincipal.create(1L, "aman@test.com", "pass", "CUSTOMER");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(workerRepository.findById(2L)).thenReturn(Optional.of(sampleWorker));
        when(jobRequestRepository.save(any(JobRequest.class))).thenReturn(sampleJob);

        JobResponseDto response = jobRequestService.createJobRequest(dto, principal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(JobStatus.REQUESTED);
        verify(jobRequestRepository).save(any(JobRequest.class));
    }

    @Test
    @DisplayName("Should reject job creation when worker is unavailable")
    void createJobRequest_WorkerUnavailable_ThrowsException() {
        sampleWorker.setAvailable(false);

        CreateJobRequestDto dto = CreateJobRequestDto.builder()
                .customerId(1L)
                .workerId(2L)
                .serviceType("Carpenter")
                .location("Delhi")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(workerRepository.findById(2L)).thenReturn(Optional.of(sampleWorker));

        assertThatThrownBy(() -> jobRequestService.createJobRequest(dto, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Worker is currently unavailable");

        verify(jobRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should accept valid state transition REQUESTED -> ACCEPTED and mark worker unavailable")
    void updateJobStatus_RequestedToAccepted_Success() {
        when(jobRequestRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(sampleJob));
        when(jobRequestRepository.save(any(JobRequest.class))).thenReturn(sampleJob);

        JobResponseDto updated = jobRequestService.updateJobStatus(10L, JobStatus.ACCEPTED, null);

        assertThat(updated).isNotNull();
        assertThat(sampleJob.getStatus()).isEqualTo(JobStatus.ACCEPTED);
        verify(workerService).setAvailability(2L, false);
    }

    @Test
    @DisplayName("Should mark worker available again when job reaches COMPLETED")
    void updateJobStatus_InProgressToCompleted_Success() {
        sampleJob.setStatus(JobStatus.IN_PROGRESS);

        when(jobRequestRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(sampleJob));
        when(jobRequestRepository.save(any(JobRequest.class))).thenReturn(sampleJob);

        JobResponseDto updated = jobRequestService.updateJobStatus(10L, JobStatus.COMPLETED, null);

        assertThat(updated).isNotNull();
        assertThat(sampleJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
        verify(workerService).setAvailability(2L, true);
    }

    @Test
    @DisplayName("Should reject illegal state transition directly from REQUESTED to COMPLETED")
    void updateJobStatus_IllegalTransition_ThrowsException() {
        when(jobRequestRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(sampleJob));

        assertThatThrownBy(() -> jobRequestService.updateJobStatus(10L, JobStatus.COMPLETED, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Job in REQUESTED status can only transition to ACCEPTED or CANCELLED");

        verify(jobRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject modification of COMPLETED job")
    void updateJobStatus_CompletedTerminalState_ThrowsException() {
        sampleJob.setStatus(JobStatus.COMPLETED);
        when(jobRequestRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(sampleJob));

        assertThatThrownBy(() -> jobRequestService.updateJobStatus(10L, JobStatus.IN_PROGRESS, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Cannot modify a completed job request");
    }
}
