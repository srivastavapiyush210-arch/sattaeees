package com.sattaees.sattaees.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workers")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String skill;
    private int experience;
    private String city;
    private boolean available;
    
    // Premium fields
    private Double hourlyRate;
    private Double averageRating;
    private Integer totalReviews;

    public Worker() {}

    public Worker(Long id, String name, String email, String password, String phoneNumber, String skill, int experience, String city, boolean available, Double hourlyRate, Double averageRating, Integer totalReviews) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.skill = skill;
        this.experience = experience;
        this.city = city;
        this.available = available;
        this.hourlyRate = hourlyRate;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getSkill() { return skill; }
    public int getExperience() { return experience; }
    public String getCity() { return city; }
    public boolean isAvailable() { return available; }
    public Double getHourlyRate() { return hourlyRate; }
    public Double getAverageRating() { return averageRating; }
    public Integer getTotalReviews() { return totalReviews; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setSkill(String skill) { this.skill = skill; }
    public void setExperience(int experience) { this.experience = experience; }
    public void setCity(String city) { this.city = city; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
}
