package com.gymbooking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record GymClassRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String instructor,
        @NotNull @Future LocalDateTime startTime,
        @NotNull @Min(15) @Max(240) Integer durationMinutes,
        @NotNull @Min(1) @Max(100) Integer capacity
) {
}
