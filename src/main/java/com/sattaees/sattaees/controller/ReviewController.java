package com.sattaees.sattaees.controller;

import com.sattaees.sattaees.model.Review;
import com.sattaees.sattaees.model.Worker;
import com.sattaees.sattaees.model.Customer;
import com.sattaees.sattaees.repository.ReviewRepository;
import com.sattaees.sattaees.repository.WorkerRepository;
import com.sattaees.sattaees.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final WorkerRepository workerRepository;
    private final CustomerRepository customerRepository;

    public ReviewController(ReviewRepository reviewRepository, WorkerRepository workerRepository, CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.workerRepository = workerRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<List<Review>> getReviewsForWorker(@PathVariable Long workerId) {
        return ResponseEntity.ok(reviewRepository.findByWorkerId(workerId));
    }

    @PostMapping
    public ResponseEntity<?> leaveReview(
            @RequestParam Long customerId, 
            @RequestParam Long workerId, 
            @RequestParam Integer rating, 
            @RequestParam(required = false) String comment) {
            
        Worker w = workerRepository.findById(workerId).orElse(null);
        Customer c = customerRepository.findById(customerId).orElse(null);
        if(w == null || c == null) return ResponseEntity.badRequest().body("Invalid IDs");
        
        Review review = new Review(null, w, c, rating, comment);
        reviewRepository.save(review);

        int currentTotal = w.getTotalReviews() == null ? 0 : w.getTotalReviews();
        double currentAvg = w.getAverageRating() == null ? 0.0 : w.getAverageRating();
        
        double newAvg = ((currentAvg * currentTotal) + rating) / (currentTotal + 1);
        w.setTotalReviews(currentTotal + 1);
        w.setAverageRating(Math.round(newAvg * 10.0) / 10.0);
        workerRepository.save(w);

        return ResponseEntity.ok(review);
    }
}
