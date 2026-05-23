package com.sattaees.sattaees.service;

import com.sattaees.sattaees.model.Worker;
import com.sattaees.sattaees.repository.WorkerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public WorkerService(WorkerRepository workerRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.workerRepository = workerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // CREATE
    public Worker createWorker(Worker worker) {
        worker.setPassword(passwordEncoder.encode(worker.getPassword()));
        return workerRepository.save(worker);
    }
    
    public Worker loginWorker(String email, String password) {
        return workerRepository.findByEmail(email)
            .filter(w -> passwordEncoder.matches(password, w.getPassword()))
            .orElse(null);
    }

    // READ ALL
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

    // READ BY ID
    public Worker getWorkerById(Long id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found with id " + id));
    }

    // UPDATE
    public Worker updateWorker(Long id, Worker updatedWorker) {

        Worker existingWorker = workerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found with id " + id));

        existingWorker.setName(updatedWorker.getName());
        existingWorker.setPhoneNumber(updatedWorker.getPhoneNumber());
        existingWorker.setSkill(updatedWorker.getSkill());
        existingWorker.setExperience(updatedWorker.getExperience());
        existingWorker.setCity(updatedWorker.getCity());
        existingWorker.setAvailable(updatedWorker.isAvailable());

        return workerRepository.save(existingWorker);
    }

    // DELETE
    public void deleteWorker(Long id) {
        workerRepository.deleteById(id);
    }

    // SEARCH BY SKILL
    public List<Worker> findWorkersBySkill(String skill) {
        return workerRepository.findBySkill(skill);
    }

    // SEARCH BY SKILL AND CITY
    public List<Worker> findWorkersBySkillAndCity(String skill, String city) {
        return workerRepository.findBySkillAndCity(skill, city);
    }

    // PAGINATION
    public Page<Worker> getWorkersPaged(Pageable pageable) {
        return workerRepository.findAll(pageable);
    }
}