package com.kaminski.FinanceApp.exception;

// Error 409
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
