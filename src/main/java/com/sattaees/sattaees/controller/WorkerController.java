package com.sattaees.sattaees.controller;

import com.sattaees.sattaees.dto.AuthResponse;
import com.sattaees.sattaees.dto.LoginRequest;
import com.sattaees.sattaees.model.Worker;
import com.sattaees.sattaees.service.WorkerService;
import com.sattaees.sattaees.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService workerService;
    private final JwtUtil jwtUtil;

    public WorkerController(WorkerService workerService, JwtUtil jwtUtil) {
        this.workerService = workerService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> createWorker(@Valid @RequestBody Worker worker) {
        log.info("REST request to register a new worker: {}", worker.getEmail());
        Worker savedWorker = workerService.createWorker(worker);
        String token = jwtUtil.generateToken(savedWorker.getEmail(), "WORKER", savedWorker.getId());
        return ResponseEntity.ok(new AuthResponse(token, savedWorker));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        log.info("REST request to login worker: {}", req.getEmail());
        Worker w = workerService.loginWorker(req.getEmail(), req.getPassword());
        if (w != null) {
            String token = jwtUtil.generateToken(w.getEmail(), "WORKER", w.getId());
            return ResponseEntity.ok(new AuthResponse(token, w));
        }
        log.warn("Worker login failed for: {}", req.getEmail());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @GetMapping
    public ResponseEntity<List<Worker>> getAllWorkers() {
        log.info("REST request to get all workers");
        return ResponseEntity.ok(workerService.getAllWorkers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Worker> getWorkerById(@PathVariable Long id) {
        log.info("REST request to get worker details for ID: {}", id);
        return ResponseEntity.ok(workerService.getWorkerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Worker> updateWorker(@PathVariable Long id, @Valid @RequestBody Worker worker) {
        log.info("REST request to update worker ID: {}", id);
        return ResponseEntity.ok(workerService.updateWorker(id, worker));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorker(@PathVariable Long id) {
        log.info("REST request to delete worker ID: {}", id);
        workerService.deleteWorker(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Worker>> searchBySkill(@RequestParam String skill) {
        log.info("REST request to search workers by skill: {}", skill);
        return ResponseEntity.ok(workerService.findWorkersBySkill(skill));
    }

    @GetMapping("/search-by-city")
    public ResponseEntity<List<Worker>> searchBySkillAndCity(
            @RequestParam String skill,
            @RequestParam String city) {
        log.info("REST request to search workers by skill: {} in city: {}", skill, city);
        return ResponseEntity.ok(workerService.findWorkersBySkillAndCity(skill, city));
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Worker>> getWorkersPaged(Pageable pageable) {
        log.info("REST request to get workers paged: {}", pageable);
        return ResponseEntity.ok(workerService.getWorkersPaged(pageable));
    }
}