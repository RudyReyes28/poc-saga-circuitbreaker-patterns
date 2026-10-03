package com.alessandro.payment_service.dto;


import com.alessandro.payment_service.anums.PaymentStatus;
import com.alessandro.payment_service.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        String customerId,
        BigDecimal amount,
        PaymentStatus status,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getCustomerId(),
                p.getAmount(), p.getStatus(), p.getCreatedAt());
    }
}