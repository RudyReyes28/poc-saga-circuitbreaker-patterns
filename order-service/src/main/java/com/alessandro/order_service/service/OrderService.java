package com.alessandro.order_service.service;


import com.alessandro.order_service.dto.CreateOrderRequest;
import com.alessandro.order_service.dto.OrderResponse;
import com.alessandro.order_service.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrder(UUID id);

    List<OrderResponse> getAllOrders();

    // Lo usará el orquestador de la saga para avanzar o compensar
    OrderResponse updateStatus(UUID id, OrderStatus status, String failureReason);

    OrderResponse markPaid(UUID id, UUID paymentId);

    OrderResponse markCompleted(UUID id, UUID shippingId);

}