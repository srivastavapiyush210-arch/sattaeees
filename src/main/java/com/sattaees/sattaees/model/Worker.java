package com.sattaees.sattaees.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workers")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String phoneNumber;

    private String skill;

    private int experience;

    private String city;

    private boolean available;

    public Worker() {}

    public Worker(Long id, String name, String phoneNumber, String skill, int experience, String city, boolean available) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.skill = skill;
        this.experience = experience;
        this.city = city;
        this.available = available;
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getPhoneNumber() { return phoneNumber; }

    public String getSkill() { return skill; }

    public int getExperience() { return experience; }

    public String getCity() { return city; }

    public boolean isAvailable() { return available; }

    public void setId(Long id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public void setSkill(String skill) { this.skill = skill; }

    public void setExperience(int experience) { this.experience = experience; }

    public void setCity(String city) { this.city = city; }

    public void setAvailable(boolean available) { this.available = available; }
}
