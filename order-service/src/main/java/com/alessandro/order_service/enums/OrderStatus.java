package com.alessandro.order_service.enums;

public enum OrderStatus {
    PENDING,         // orden creada, saga iniciada
    PAID,            // pago aprobado
    STOCK_RESERVED,  // stock reservado
    COMPLETED,       // envío programado, saga terminada
    CANCELLED,        // saga compensada
    COMPENSATION_FAILED   // la compensación no pudo completarse
}