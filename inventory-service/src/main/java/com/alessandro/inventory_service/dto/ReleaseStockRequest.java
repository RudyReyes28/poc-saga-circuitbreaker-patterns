package com.alessandro.inventory_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReleaseStockRequest(@NotNull UUID orderId) {}