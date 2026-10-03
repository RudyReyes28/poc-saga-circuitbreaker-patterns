package com.alessandro.order_service.messaging;

import java.util.UUID;

public record ShippingCommand(
        Action action,
        UUID orderId,
        String customerId,
        String productId,
        Integer quantity
) {
    public enum Action { SCHEDULE, CANCEL }
}