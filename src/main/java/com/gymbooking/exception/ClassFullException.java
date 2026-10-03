package com.gymbooking.exception;

public class ClassFullException extends BusinessRuleException {

    public ClassFullException(String className) {
        super("Class '" + className + "' is fully booked");
    }
}
