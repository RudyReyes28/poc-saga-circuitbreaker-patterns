package com.alessandro.inventory_service.exception;

import java.util.UUID;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(UUID orderId) {
        super("No existe reserva para la orden: " + orderId);
    }
}