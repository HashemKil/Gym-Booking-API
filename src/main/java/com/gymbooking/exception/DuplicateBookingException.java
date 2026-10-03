package com.gymbooking.exception;

public class DuplicateBookingException extends BusinessRuleException {

    public DuplicateBookingException(String memberName, String className) {
        super(memberName + " is already booked into '" + className + "'");
    }
}
