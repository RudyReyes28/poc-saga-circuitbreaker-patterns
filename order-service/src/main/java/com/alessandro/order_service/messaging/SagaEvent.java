package com.alessandro.order_service.messaging;


import java.time.Instant;
import java.util.UUID;

public record SagaEvent(UUID orderId, String event, String detail, Instant occurredAt) {}