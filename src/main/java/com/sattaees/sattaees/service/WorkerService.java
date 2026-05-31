package com.sattaees.sattaees.service;

import com.sattaees.sattaees.exception.DuplicateResourceException;
import com.sattaees.sattaees.exception.ResourceNotFoundException;
import com.sattaees.sattaees.model.Worker;
import com.sattaees.sattaees.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;

    public WorkerService(WorkerRepository workerRepository, PasswordEncoder passwordEncoder) {
        this.workerRepository = workerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Worker createWorker(Worker worker) {
        log.info("Attempting to register worker with email: {}", worker.getEmail());
        if (workerRepository.findByEmail(worker.getEmail()).isPresent()) {
            log.warn("Worker registration failed: Email {} is already taken", worker.getEmail());
            throw new DuplicateResourceException("Email address already registered: " + worker.getEmail());
        }
        worker.setPassword(passwordEncoder.encode(worker.getPassword()));
        Worker savedWorker = workerRepository.save(worker);
        log.info("Worker registered successfully with ID: {}", savedWorker.getId());
        return savedWorker;
    }
    
    public Worker loginWorker(String email, String password) {
        log.info("Processing login request for worker: {}", email);
        return workerRepository.findByEmail(email)
            .filter(w -> {
                boolean match = passwordEncoder.matches(password, w.getPassword());
                if (match) {
                    log.info("Login successful for worker: {}", email);
                } else {
                    log.warn("Login failed for worker: {} (password mismatch)", email);
                }
                return match;
            })
            .orElseGet(() -> {
                log.warn("Login failed: Worker with email {} not found", email);
                return null;
            });
    }

    public List<Worker> getAllWorkers() {
        log.debug("Fetching all workers");
        return workerRepository.findAll();
    }

    public Worker getWorkerById(Long id) {
        log.debug("Fetching worker profile by ID: {}", id);
        return workerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Worker lookup failed: ID {} not found", id);
                    return new ResourceNotFoundException("Worker not found with id " + id);
                });
    }

    public Worker updateWorker(Long id, Worker updatedWorker) {
        log.info("Updating worker details for ID: {}", id);
        Worker existingWorker = workerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Worker update failed: ID {} not found", id);
                    return new ResourceNotFoundException("Worker not found with id " + id);
                });

        existingWorker.setName(updatedWorker.getName());
        existingWorker.setPhoneNumber(updatedWorker.getPhoneNumber());
        existingWorker.setSkill(updatedWorker.getSkill());
        existingWorker.setExperience(updatedWorker.getExperience());
        existingWorker.setCity(updatedWorker.getCity());
        existingWorker.setAvailable(updatedWorker.isAvailable());

        Worker savedWorker = workerRepository.save(existingWorker);
        log.info("Worker details updated successfully for ID: {}", id);
        return savedWorker;
    }

    public void deleteWorker(Long id) {
        log.info("Request to delete worker ID: {}", id);
        if (!workerRepository.existsById(id)) {
            log.warn("Worker deletion failed: ID {} not found", id);
            throw new ResourceNotFoundException("Worker not found with id " + id);
        }
        workerRepository.deleteById(id);
        log.info("Worker ID: {} deleted successfully", id);
    }

    public List<Worker> findWorkersBySkill(String skill) {
        log.debug("Searching workers by skill: {}", skill);
        return workerRepository.findBySkill(skill);
    }

    public List<Worker> findWorkersBySkillAndCity(String skill, String city) {
        log.debug("Searching workers by skill: {} and city: {}", skill, city);
        return workerRepository.findBySkillAndCity(skill, city);
    }

    public Page<Worker> getWorkersPaged(Pageable pageable) {
        log.debug("Fetching workers paged: {}", pageable);
        return workerRepository.findAll(pageable);
    }
}