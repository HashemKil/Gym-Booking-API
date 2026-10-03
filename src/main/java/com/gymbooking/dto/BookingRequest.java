package com.gymbooking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingRequest(
        @NotNull @Positive Long memberId,
        @NotNull @Positive Long classId
) {
}
