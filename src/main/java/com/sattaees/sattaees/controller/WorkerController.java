package com.sattaees.sattaees.controller;

import com.sattaees.sattaees.model.Worker;
import com.sattaees.sattaees.service.WorkerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    // CREATE WORKER
    @PostMapping
    public ResponseEntity<Worker> createWorker(@RequestBody Worker worker) {
        Worker savedWorker = workerService.createWorker(worker);
        return ResponseEntity.ok(savedWorker);
    }

    // GET ALL WORKERS
    @GetMapping
    public ResponseEntity<List<Worker>> getAllWorkers() {
        return ResponseEntity.ok(workerService.getAllWorkers());
    }

    // GET WORKER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Worker> getWorkerById(@PathVariable Long id) {
        return ResponseEntity.ok(workerService.getWorkerById(id));
    }

    // UPDATE WORKER
    @PutMapping("/{id}")
    public ResponseEntity<Worker> updateWorker(@PathVariable Long id, @RequestBody Worker worker) {
        return ResponseEntity.ok(workerService.updateWorker(id, worker));
    }

    // DELETE WORKER
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorker(@PathVariable Long id) {
        workerService.deleteWorker(id);
        return ResponseEntity.noContent().build();
    }

    // SEARCH WORKERS BY SKILL
    @GetMapping("/search")
    public ResponseEntity<List<Worker>> searchBySkill(@RequestParam String skill) {
        return ResponseEntity.ok(workerService.findWorkersBySkill(skill));
    }

    // SEARCH WORKERS BY SKILL AND CITY
    @GetMapping("/search-by-city")
    public ResponseEntity<List<Worker>> searchBySkillAndCity(
            @RequestParam String skill,
            @RequestParam String city) {

        return ResponseEntity.ok(workerService.findWorkersBySkillAndCity(skill, city));
    }

    // PAGINATION API
    @GetMapping("/paged")
    public ResponseEntity<Page<Worker>> getWorkersPaged(Pageable pageable) {
        return ResponseEntity.ok(workerService.getWorkersPaged(pageable));
    }
}