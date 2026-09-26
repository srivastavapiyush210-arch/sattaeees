package com.sattaees.sattaees.worker.repository;

import com.sattaees.sattaees.worker.entity.Worker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {

    Optional<Worker> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT w FROM Worker w WHERE LOWER(w.skill) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<Worker> findBySkill(@Param("skill") String skill);

    @Query("SELECT w FROM Worker w WHERE LOWER(w.skill) LIKE LOWER(CONCAT('%', :skill, '%')) AND LOWER(w.city) = LOWER(:city)")
    List<Worker> findBySkillAndCity(@Param("skill") String skill, @Param("city") String city);

    Page<Worker> findByAvailableTrue(Pageable pageable);

    @Query("SELECT w FROM Worker w WHERE " +
           "(:skill IS NULL OR LOWER(w.skill) LIKE LOWER(CONCAT('%', :skill, '%'))) AND " +
           "(:city IS NULL OR LOWER(w.city) = LOWER(:city)) AND " +
           "(:available IS NULL OR w.available = :available)")
    Page<Worker> searchWorkers(
            @Param("skill") String skill,
            @Param("city") String city,
            @Param("available") Boolean available,
            Pageable pageable);
}
