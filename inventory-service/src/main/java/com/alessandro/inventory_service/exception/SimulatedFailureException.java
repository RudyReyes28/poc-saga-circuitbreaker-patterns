package com.alessandro.inventory_service.exception;

public class SimulatedFailureException extends RuntimeException {
    public SimulatedFailureException(String message) {
        super(message);
    }
}