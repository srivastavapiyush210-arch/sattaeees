package com.sattaees.sattaees.worker.service;

import com.sattaees.sattaees.auth.dto.RegisterWorkerRequest;
import com.sattaees.sattaees.common.dto.PageResponse;
import com.sattaees.sattaees.common.exception.DuplicateResourceException;
import com.sattaees.sattaees.common.exception.ResourceNotFoundException;
import com.sattaees.sattaees.common.exception.UnauthorizedException;
import com.sattaees.sattaees.infrastructure.config.RedisConfig;
import com.sattaees.sattaees.infrastructure.security.UserPrincipal;
import com.sattaees.sattaees.worker.dto.WorkerResponseDto;
import com.sattaees.sattaees.worker.dto.WorkerUpdateDto;
import com.sattaees.sattaees.worker.entity.Worker;
import com.sattaees.sattaees.worker.mapper.WorkerMapper;
import com.sattaees.sattaees.worker.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing Worker catalog, profiles, search, and cached reads.
 */
@Slf4j
@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;

    public WorkerService(WorkerRepository workerRepository, PasswordEncoder passwordEncoder) {
        this.workerRepository = workerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @CacheEvict(value = {RedisConfig.CACHE_WORKER_CATALOG, RedisConfig.CACHE_WORKER_SEARCH}, allEntries = true)
    public WorkerResponseDto registerWorker(RegisterWorkerRequest request) {
        log.info("Registering worker with email: {}", request.getEmail());
        if (workerRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration conflict: Worker email {} already exists", request.getEmail());
            throw new DuplicateResourceException("Worker email address already registered: " + request.getEmail());
        }

        Worker worker = Worker.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .skill(request.getSkill())
                .experience(request.getExperience())
                .city(request.getCity())
                .available(true)
                .hourlyRate(request.getHourlyRate())
                .averageRating(0.0)
                .totalReviews(0)
                .build();

        Worker savedWorker = workerRepository.save(worker);
        log.info("Registered worker with ID: {}", savedWorker.getId());
        return WorkerMapper.toResponseDto(savedWorker);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_WORKER_PROFILE, key = "#id")
    public WorkerResponseDto getWorkerById(Long id) {
        log.debug("Cache miss - fetching worker from DB by ID: {}", id);
        Worker worker = findWorkerEntityById(id);
        return WorkerMapper.toResponseDto(worker);
    }

    @Transactional(readOnly = true)
    public Worker findWorkerEntityById(Long id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with id: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_WORKER_CATALOG, unless = "#result == null || #result.isEmpty()")
    public List<WorkerResponseDto> getAllWorkers() {
        log.debug("Cache miss - fetching all workers from DB");
        return workerRepository.findAll().stream()
                .map(WorkerMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkerResponseDto> findWorkersBySkill(String skill) {
        log.debug("Searching workers by skill: {}", skill);
        return workerRepository.findBySkill(skill).stream()
                .map(WorkerMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_WORKER_SEARCH, key = "#skill + '_' + #city", unless = "#result == null || #result.isEmpty()")
    public List<WorkerResponseDto> findWorkersBySkillAndCity(String skill, String city) {
        log.debug("Cache miss - searching workers by skill: {} and city: {}", skill, city);
        return workerRepository.findBySkillAndCity(skill, city).stream()
                .map(WorkerMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<WorkerResponseDto> getWorkersPaged(Pageable pageable) {
        log.debug("Fetching paged workers: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Worker> page = workerRepository.findAll(pageable);
        Page<WorkerResponseDto> dtoPage = page.map(WorkerMapper::toResponseDto);
        return PageResponse.from(dtoPage);
    }

    @Transactional
    @CacheEvict(value = {RedisConfig.CACHE_WORKER_PROFILE, RedisConfig.CACHE_WORKER_CATALOG, RedisConfig.CACHE_WORKER_SEARCH}, allEntries = true)
    public WorkerResponseDto updateWorker(Long id, WorkerUpdateDto updateDto, UserPrincipal currentUser) {
        log.info("Updating worker profile for ID: {}", id);
        verifyWorkerOwnership(id, currentUser);

        Worker worker = findWorkerEntityById(id);
        worker.setName(updateDto.getName());
        worker.setPhoneNumber(updateDto.getPhoneNumber());
        worker.setSkill(updateDto.getSkill());
        if (updateDto.getExperience() != null) {
            worker.setExperience(updateDto.getExperience());
        }
        worker.setCity(updateDto.getCity());
        if (updateDto.getAvailable() != null) {
            worker.setAvailable(updateDto.getAvailable());
        }
        if (updateDto.getHourlyRate() != null) {
            worker.setHourlyRate(updateDto.getHourlyRate());
        }

        Worker updated = workerRepository.save(worker);
        log.info("Successfully updated worker profile ID: {}", id);
        return WorkerMapper.toResponseDto(updated);
    }

    @Transactional
    @CacheEvict(value = {RedisConfig.CACHE_WORKER_PROFILE, RedisConfig.CACHE_WORKER_CATALOG, RedisConfig.CACHE_WORKER_SEARCH}, allEntries = true)
    public void deleteWorker(Long id, UserPrincipal currentUser) {
        log.info("Request to delete worker ID: {}", id);
        verifyWorkerOwnership(id, currentUser);

        if (!workerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Worker not found with id: " + id);
        }
        workerRepository.deleteById(id);
        log.info("Deleted worker ID: {}", id);
    }

    @Transactional
    @CacheEvict(value = {RedisConfig.CACHE_WORKER_PROFILE, RedisConfig.CACHE_WORKER_CATALOG, RedisConfig.CACHE_WORKER_SEARCH}, allEntries = true)
    public void setAvailability(Long workerId, boolean available) {
        Worker worker = findWorkerEntityById(workerId);
        worker.setAvailable(available);
        workerRepository.save(worker);
        log.info("Updated worker ID: {} availability to: {}", workerId, available);
    }

    /**
     * Updates worker rating and total reviews count using optimistic locking.
     */
    @Transactional
    @CacheEvict(value = {RedisConfig.CACHE_WORKER_PROFILE, RedisConfig.CACHE_WORKER_CATALOG, RedisConfig.CACHE_WORKER_SEARCH}, allEntries = true)
    public void updateWorkerRating(Long workerId, int newRating) {
        Worker worker = findWorkerEntityById(workerId);
        int currentCount = worker.getTotalReviews() == null ? 0 : worker.getTotalReviews();
        double currentAvg = worker.getAverageRating() == null ? 0.0 : worker.getAverageRating();

        double newAvg = ((currentAvg * currentCount) + newRating) / (currentCount + 1);
        worker.setTotalReviews(currentCount + 1);
        worker.setAverageRating(Math.round(newAvg * 10.0) / 10.0);

        workerRepository.save(worker);
        log.info("Updated worker ID: {} new average rating: {}, total reviews: {}", workerId, worker.getAverageRating(), worker.getTotalReviews());
    }

    private void verifyWorkerOwnership(Long id, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required.");
        }
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = currentUser.getId().equals(id) && "WORKER".equalsIgnoreCase(currentUser.getRole());

        if (!isAdmin && !isOwner) {
            log.warn("Access denied: User {} tried modifying worker {}", currentUser.getEmail(), id);
            throw new UnauthorizedException("You are not authorized to modify this worker profile.");
        }
    }
}
