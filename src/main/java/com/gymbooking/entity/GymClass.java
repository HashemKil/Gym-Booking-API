package com.gymbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "gym_classes")
public class GymClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String instructor;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private int capacity;

    protected GymClass() {
    }

    public GymClass(String name, String instructor, LocalDateTime startTime, int durationMinutes, int capacity) {
        this.name = name;
        this.instructor = instructor;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
    }

    public void update(String name, String instructor, LocalDateTime startTime, int durationMinutes, int capacity) {
        this.name = name;
        this.instructor = instructor;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
    }

    public boolean hasStarted(LocalDateTime now) {
        return !startTime.isAfter(now);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInstructor() {
        return instructor;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getCapacity() {
        return capacity;
    }
}
