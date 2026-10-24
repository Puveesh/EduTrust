package com.sentinel.ledger.exception;

public class IntakeExceededException extends RuntimeException {
    public IntakeExceededException(String message) {
        super(message);
    }
}
