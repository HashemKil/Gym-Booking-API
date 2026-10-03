package com.gymbooking.exception;

public class ClassAlreadyStartedException extends BusinessRuleException {

    public ClassAlreadyStartedException(String className) {
        super("Class '" + className + "' has already started");
    }
}
