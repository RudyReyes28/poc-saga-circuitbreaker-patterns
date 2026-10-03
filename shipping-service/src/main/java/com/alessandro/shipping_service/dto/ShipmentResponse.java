package com.alessandro.shipping_service.dto;

import com.alessandro.shipping_service.entity.Shipment;
import com.alessandro.shipping_service.enums.ShippingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        UUID orderId,
        String trackingCode,
        ShippingStatus status,
        LocalDateTime createdAt
) {
    public static ShipmentResponse from(Shipment s) {
        return new ShipmentResponse(s.getId(), s.getOrderId(), s.getTrackingCode(),
                s.getStatus(), s.getCreatedAt());
    }
}