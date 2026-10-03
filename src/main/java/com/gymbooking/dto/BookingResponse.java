package com.gymbooking.dto;

import com.gymbooking.entity.Booking;
import com.gymbooking.entity.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long memberId,
        String memberName,
        Long classId,
        String className,
        LocalDateTime classStartTime,
        BookingStatus status,
        LocalDateTime bookedAt,
        LocalDateTime cancelledAt
) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getMember().getId(),
                booking.getMember().getFullName(),
                booking.getGymClass().getId(),
                booking.getGymClass().getName(),
                booking.getGymClass().getStartTime(),
                booking.getStatus(),
                booking.getBookedAt(),
                booking.getCancelledAt()
        );
    }
}
