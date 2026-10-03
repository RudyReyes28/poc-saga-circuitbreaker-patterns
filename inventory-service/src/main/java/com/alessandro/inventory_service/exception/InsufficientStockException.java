package com.alessandro.inventory_service.exception;


public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productId, int requested) {
        super("Stock insuficiente para " + productId + " (solicitado: " + requested + ")");
    }
}