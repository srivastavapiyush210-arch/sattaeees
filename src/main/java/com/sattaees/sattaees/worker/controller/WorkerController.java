package com.sattaees.sattaees.worker.controller;

import com.sattaees.sattaees.auth.dto.AuthResponse;
import com.sattaees.sattaees.auth.dto.LoginRequest;
import com.sattaees.sattaees.auth.dto.RegisterWorkerRequest;
import com.sattaees.sattaees.auth.service.AuthService;
import com.sattaees.sattaees.common.dto.PageResponse;
import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import com.sattaees.sattaees.worker.dto.WorkerResponseDto;
import com.sattaees.sattaees.worker.dto.WorkerUpdateDto;
import com.sattaees.sattaees.worker.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/workers")
@Tag(name = "Worker Management", description = "Endpoints for worker catalog, search, profiles, and service expert operations")
public class WorkerController {

    private final WorkerService workerService;
    private final AuthService authService;

    public WorkerController(WorkerService workerService, AuthService authService) {
        this.workerService = workerService;
        this.authService = authService;
    }

    @PostMapping
    @Operation(summary = "Register a worker and return auth credentials (Compatible with frontend)")
    public ResponseEntity<AuthResponse> createWorker(@Valid @RequestBody RegisterWorkerRequest request) {
        log.info("REST request to register worker: {}", request.getEmail());
        WorkerResponseDto worker = workerService.registerWorker(request);
        AuthResponse response = authService.createAuthResponse(worker.getId(), worker.getEmail(), worker.getName(), "WORKER");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Worker login (Compatible with frontend)")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login worker: {}", request.getEmail());
        return ResponseEntity.ok(authService.loginWorker(request));
    }

    @GetMapping
    @Operation(summary = "Get all workers catalog (Cached in Redis)")
    public ResponseEntity<List<WorkerResponseDto>> getAllWorkers() {
        log.info("REST request to get all workers");
        return ResponseEntity.ok(workerService.getAllWorkers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get worker details by ID (Cached in Redis)")
    public ResponseEntity<WorkerResponseDto> getWorkerById(@PathVariable Long id) {
        log.info("REST request to get worker ID: {}", id);
        return ResponseEntity.ok(workerService.getWorkerById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update worker profile")
    public ResponseEntity<WorkerResponseDto> updateWorker(
            @PathVariable Long id,
            @Valid @RequestBody WorkerUpdateDto updateDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST request to update worker ID: {}", id);
        return ResponseEntity.ok(workerService.updateWorker(id, updateDto, currentUser));
    }

    @PatchMapping("/{id}/availability")
    @Operation(summary = "Toggle worker availability status")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST request to set worker ID: {} availability to: {}", id, available);
        workerService.setAvailability(id, available);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete worker account")
    public ResponseEntity<Void> deleteWorker(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST request to delete worker ID: {}", id);
        workerService.deleteWorker(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search workers by skill")
    public ResponseEntity<List<WorkerResponseDto>> searchBySkill(@RequestParam String skill) {
        log.info("REST request to search workers by skill: {}", skill);
        return ResponseEntity.ok(workerService.findWorkersBySkill(skill));
    }

    @GetMapping("/search-by-city")
    @Operation(summary = "Search workers by skill and city (Cached in Redis)")
    public ResponseEntity<List<WorkerResponseDto>> searchBySkillAndCity(
            @RequestParam String skill,
            @RequestParam String city) {
        log.info("REST request to search workers by skill: {} and city: {}", skill, city);
        return ResponseEntity.ok(workerService.findWorkersBySkillAndCity(skill, city));
    }

    @GetMapping("/paged")
    @Operation(summary = "Get paginated workers with sorting and page metadata")
    public ResponseEntity<PageResponse<WorkerResponseDto>> getWorkersPaged(
            @PageableDefault(size = 10, sort = "averageRating") Pageable pageable) {
        log.info("REST request for paged workers: {}", pageable);
        return ResponseEntity.ok(workerService.getWorkersPaged(pageable));
    }
}
