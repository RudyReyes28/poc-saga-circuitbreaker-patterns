package com.alessandro.inventory_service.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Producto no encontrado: " + productId);
    }
}