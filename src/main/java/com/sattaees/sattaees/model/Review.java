package com.sattaees.sattaees.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private Integer rating;
    private String comment;

    public Review() {}

    public Review(Long id, Worker worker, Customer customer, Integer rating, String comment) {
        this.id = id;
        this.worker = worker;
        this.customer = customer;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public Worker getWorker() { return worker; }
    public Customer getCustomer() { return customer; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }

    public void setId(Long id) { this.id = id; }
    public void setWorker(Worker worker) { this.worker = worker; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setRating(Integer rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
}
