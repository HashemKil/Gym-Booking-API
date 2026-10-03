package com.gymbooking.exception;

public class EmailAlreadyUsedException extends BusinessRuleException {

    public EmailAlreadyUsedException(String email) {
        super("A member with email " + email + " already exists");
    }
}
