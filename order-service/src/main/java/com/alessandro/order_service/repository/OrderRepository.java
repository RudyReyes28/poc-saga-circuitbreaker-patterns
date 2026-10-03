package com.alessandro.order_service.repository;

import com.alessandro.order_service.entity.Order;
import com.alessandro.order_service.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerId(String customerId);
}