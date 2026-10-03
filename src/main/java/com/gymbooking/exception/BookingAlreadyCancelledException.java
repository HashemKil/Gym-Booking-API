package com.gymbooking.exception;

public class BookingAlreadyCancelledException extends BusinessRuleException {

    public BookingAlreadyCancelledException(Long bookingId) {
        super("Booking " + bookingId + " is already cancelled");
    }
}
