package com.alessandro.shipping_service.exception;


public class SimulatedFailureException extends RuntimeException {
    public SimulatedFailureException(String message) {
        super(message);
    }
}