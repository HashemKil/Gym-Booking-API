package com.gymbooking.dto;

import com.gymbooking.entity.GymClass;

import java.time.LocalDateTime;

public record GymClassResponse(
        Long id,
        String name,
        String instructor,
        LocalDateTime startTime,
        int durationMinutes,
        int capacity,
        int bookedCount,
        int spotsLeft
) {

    public static GymClassResponse from(GymClass gymClass, long bookedCount) {
        int booked = (int) bookedCount;
        return new GymClassResponse(
                gymClass.getId(),
                gymClass.getName(),
                gymClass.getInstructor(),
                gymClass.getStartTime(),
                gymClass.getDurationMinutes(),
                gymClass.getCapacity(),
                booked,
                Math.max(gymClass.getCapacity() - booked, 0)
        );
    }
}
