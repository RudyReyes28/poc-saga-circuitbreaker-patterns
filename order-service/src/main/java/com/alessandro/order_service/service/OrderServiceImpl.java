package com.alessandro.order_service.service;


import com.alessandro.order_service.dto.CreateOrderRequest;
import com.alessandro.order_service.dto.OrderResponse;
import com.alessandro.order_service.entity.Order;
import com.alessandro.order_service.enums.OrderStatus;
import com.alessandro.order_service.exception.OrderNotFoundException;
import com.alessandro.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.customerId());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalAmount(request.unitPrice().multiply(BigDecimal.valueOf(request.quantity())));
        order.setStatus(OrderStatus.PENDING);

        return OrderResponse.from(orderRepository.save(order));
    }

    @Override
    public OrderResponse getOrder(UUID id) {
        return OrderResponse.from(findOrThrow(id));
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus status, String failureReason) {
        Order order = findOrThrow(id);
        order.setStatus(status);
        order.setFailureReason(failureReason);
        return OrderResponse.from(order);
    }

    private Order findOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional
    public OrderResponse markPaid(UUID id, UUID paymentId) {
        Order order = findOrThrow(id);
        order.setPaymentId(paymentId);
        order.setStatus(OrderStatus.PAID);
        return OrderResponse.from(order);
    }

    @Override
    @Transactional
    public OrderResponse markCompleted(UUID id, UUID shippingId) {
        Order order = findOrThrow(id);
        order.setShippingId(shippingId);
        order.setStatus(OrderStatus.COMPLETED);
        return OrderResponse.from(order);
    }
}