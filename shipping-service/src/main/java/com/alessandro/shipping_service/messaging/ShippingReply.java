package com.alessandro.shipping_service.messaging;

import java.util.UUID;

public record ShippingReply(
        UUID orderId,
        UUID shippingId,
        Status status,
        String reason
) {
    public enum Status { SCHEDULED, CANCELLED, FAILED }
}