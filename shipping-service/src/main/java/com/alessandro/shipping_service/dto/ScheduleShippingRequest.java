package com.alessandro.shipping_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ScheduleShippingRequest(
        @NotNull UUID orderId,
        @NotBlank String customerId,
        @NotBlank String productId,
        @NotNull @Min(1) Integer quantity
) {}