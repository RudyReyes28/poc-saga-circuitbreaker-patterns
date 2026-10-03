package com.alessandro.inventory_service.dto;


import com.alessandro.inventory_service.entity.Reservation;
import com.alessandro.inventory_service.enums.ReservationStatus;

import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID orderId,
        String productId,
        Integer quantity,
        ReservationStatus status
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(r.getId(), r.getOrderId(), r.getProductId(),
                r.getQuantity(), r.getStatus());
    }
}