package com.sattaees.sattaees.repository;

import com.sattaees.sattaees.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    List<Worker> findBySkillAndCity(String skill, String city);

    List<Worker> findBySkill(String skill);

}
