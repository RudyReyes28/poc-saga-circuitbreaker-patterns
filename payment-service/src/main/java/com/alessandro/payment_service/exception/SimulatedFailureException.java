package com.alessandro.payment_service.exception;

public class SimulatedFailureException extends RuntimeException {
    public SimulatedFailureException(String message) {
        super(message);
    }
}