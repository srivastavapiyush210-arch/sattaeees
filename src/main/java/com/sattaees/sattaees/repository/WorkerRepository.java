package com.sattaees.sattaees.repository;

import com.sattaees.sattaees.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    List<Worker> findBySkillAndCity(String skill, String city);

    List<Worker> findBySkill(String skill);
    
    Optional<Worker> findByEmail(String email);

}
