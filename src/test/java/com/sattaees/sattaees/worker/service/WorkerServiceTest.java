package com.sattaees.sattaees.worker.service;

import com.sattaees.sattaees.auth.dto.RegisterWorkerRequest;
import com.sattaees.sattaees.common.exception.DuplicateResourceException;
import com.sattaees.sattaees.common.exception.ResourceNotFoundException;
import com.sattaees.sattaees.common.exception.UnauthorizedException;
import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import com.sattaees.sattaees.worker.dto.WorkerResponseDto;
import com.sattaees.sattaees.worker.dto.WorkerUpdateDto;
import com.sattaees.sattaees.worker.entity.Worker;
import com.sattaees.sattaees.worker.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private WorkerService workerService;

    private Worker sampleWorker;

    @BeforeEach
    void setUp() {
        sampleWorker = Worker.builder()
                .id(1L)
                .name("Ramesh Kumar")
                .email("ramesh@test.com")
                .password("encoded_pass")
                .phoneNumber("+91 9876543210")
                .skill("Plumber")
                .experience(5)
                .city("Delhi")
                .available(true)
                .hourlyRate(40.0)
                .averageRating(4.5)
                .totalReviews(10)
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new worker")
    void registerWorker_Success() {
        RegisterWorkerRequest request = RegisterWorkerRequest.builder()
                .name("Ramesh Kumar")
                .email("ramesh@test.com")
                .password("secret123")
                .phoneNumber("+91 9876543210")
                .skill("Plumber")
                .experience(5)
                .city("Delhi")
                .hourlyRate(40.0)
                .build();

        when(workerRepository.existsByEmail("ramesh@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_pass");
        when(workerRepository.save(any(Worker.class))).thenReturn(sampleWorker);

        WorkerResponseDto response = workerService.registerWorker(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("ramesh@test.com");
        verify(workerRepository).save(any(Worker.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException if worker email already exists")
    void registerWorker_DuplicateEmail_ThrowsException() {
        RegisterWorkerRequest request = RegisterWorkerRequest.builder()
                .name("Ramesh Kumar")
                .email("ramesh@test.com")
                .password("secret123")
                .phoneNumber("+91 9876543210")
                .skill("Plumber")
                .experience(5)
                .city("Delhi")
                .hourlyRate(40.0)
                .build();

        when(workerRepository.existsByEmail("ramesh@test.com")).thenReturn(true);

        assertThatThrownBy(() -> workerService.registerWorker(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");

        verify(workerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fetch worker by ID successfully")
    void getWorkerById_Success() {
        when(workerRepository.findById(1L)).thenReturn(Optional.of(sampleWorker));

        WorkerResponseDto response = workerService.getWorkerById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Ramesh Kumar");
        assertThat(response.getSkill()).isEqualTo("Plumber");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when worker not found by ID")
    void getWorkerById_NotFound_ThrowsException() {
        when(workerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workerService.getWorkerById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Worker not found");
    }

    @Test
    @DisplayName("Should update worker profile when user is the owner")
    void updateWorker_Owner_Success() {
        UserPrincipal owner = UserPrincipal.create(1L, "ramesh@test.com", "pass", "WORKER");
        WorkerUpdateDto updateDto = WorkerUpdateDto.builder()
                .name("Ramesh K.")
                .phoneNumber("+91 9999999999")
                .skill("Master Plumber")
                .experience(6)
                .city("Noida")
                .hourlyRate(55.0)
                .build();

        when(workerRepository.findById(1L)).thenReturn(Optional.of(sampleWorker));
        when(workerRepository.save(any(Worker.class))).thenReturn(sampleWorker);

        WorkerResponseDto result = workerService.updateWorker(1L, updateDto, owner);

        assertThat(result).isNotNull();
        verify(workerRepository).save(sampleWorker);
        assertThat(sampleWorker.getName()).isEqualTo("Ramesh K.");
    }

    @Test
    @DisplayName("Should reject worker update when user is NOT the owner")
    void updateWorker_NotOwner_ThrowsUnauthorizedException() {
        UserPrincipal intruder = UserPrincipal.create(2L, "other@test.com", "pass", "WORKER");
        WorkerUpdateDto updateDto = WorkerUpdateDto.builder()
                .name("Hacker")
                .phoneNumber("+91 1111111111")
                .skill("Hacker")
                .city("Nowhere")
                .build();

        assertThatThrownBy(() -> workerService.updateWorker(1L, updateDto, intruder))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not authorized");

        verify(workerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should accurately recalculate average rating and total reviews on review update")
    void updateWorkerRating_RecalculatesAverageAccurately() {
        // Current: 10 reviews, avg 4.5 -> sum was 45.0. New rating: 5 -> new sum = 50.0 / 11 = 4.545 -> rounds to 4.5
        when(workerRepository.findById(1L)).thenReturn(Optional.of(sampleWorker));
        when(workerRepository.save(any(Worker.class))).thenReturn(sampleWorker);

        workerService.updateWorkerRating(1L, 5);

        assertThat(sampleWorker.getTotalReviews()).isEqualTo(11);
        assertThat(sampleWorker.getAverageRating()).isEqualTo(4.5);
        verify(workerRepository).save(sampleWorker);
    }
}
