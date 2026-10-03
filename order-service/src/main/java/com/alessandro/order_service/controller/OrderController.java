package com.alessandro.order_service.controller;

import com.alessandro.order_service.dto.CreateOrderRequest;
import com.alessandro.order_service.dto.OrderResponse;
import com.alessandro.order_service.enums.OrderStatus;
import com.alessandro.order_service.messaging.OrderSagaOrchestrator;
import com.alessandro.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderSagaOrchestrator sagaOrchestrator;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sagaOrchestrator.placeOrder(request));
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable UUID id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        return orderService.getAllOrders();
    }

    /** Compensación de Order. El orquestador cancela por la capa de servicio, sin pasar por HTTP. */
    @DeleteMapping("/{id}")
    public OrderResponse cancel(@PathVariable UUID id) {
        return orderService.updateStatus(id, OrderStatus.CANCELLED, "Cancelada vía DELETE /orders/{id}");
    }
}