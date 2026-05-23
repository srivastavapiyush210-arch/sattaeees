package com.sattaees.sattaees.repository;

import com.sattaees.sattaees.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByWorkerId(Long workerId);
}
