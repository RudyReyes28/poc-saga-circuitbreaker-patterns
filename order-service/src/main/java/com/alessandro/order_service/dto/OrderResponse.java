package com.alessandro.order_service.dto;


import com.alessandro.order_service.entity.Order;
import com.alessandro.order_service.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        String productId,
        Integer quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        String failureReason,
        UUID paymentId,
        UUID shippingId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(), order.getCustomerId(), order.getProductId(),
                order.getQuantity(), order.getTotalAmount(), order.getStatus(),
                order.getFailureReason(), order.getPaymentId(), order.getShippingId(),
                order.getCreatedAt(), order.getUpdatedAt());
    }
}