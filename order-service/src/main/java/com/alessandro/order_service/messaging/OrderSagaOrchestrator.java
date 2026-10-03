package com.alessandro.order_service.messaging;

import com.alessandro.order_service.client.InventoryClient;
import com.alessandro.order_service.client.PaymentClient;
import com.alessandro.order_service.client.StepResult;
import com.alessandro.order_service.dto.CreateOrderRequest;
import com.alessandro.order_service.dto.OrderResponse;
import com.alessandro.order_service.enums.OrderStatus;
import com.alessandro.order_service.exception.OrderNotFoundException;
import com.alessandro.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private static final int MAX_ATTEMPTS = 3;

    private final OrderService orderService;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    private final ShippingCommandPublisher shippingPublisher;
    private final SagaEventPublisher sagaEvents;

    //
    // Inicio de la saga (pasos síncronos: pago e inventario)
    //
    public OrderResponse placeOrder(CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        UUID orderId = order.id();
        sagaEvents.publish(orderId, "ORDER_CREATED", null);

        // Paso 1: cobro (Circuit Breaker #1)
        StepResult<PaymentClient.PaymentData> payment = paymentClient.charge(
                new PaymentClient.ChargeRequest(orderId, order.customerId(), order.totalAmount()));
        if (!payment.isSuccess()) {
            sagaEvents.publish(orderId, "PAYMENT_FAILED", payment.message());
            return cancel(orderId, "Pago no completado: " + payment.message());   // nada que compensar
        }
        order = orderService.markPaid(orderId, payment.data().id());
        sagaEvents.publish(orderId, "PAYMENT_COMPLETED", null);

        // Paso 2: reserva de stock (Circuit Breaker #2)
        StepResult<InventoryClient.ReservationData> reservation = inventoryClient.reserve(
                new InventoryClient.ReserveRequest(orderId, order.productId(), order.quantity()));
        if (!reservation.isSuccess()) {
            sagaEvents.publish(orderId, "STOCK_FAILED", reservation.message());
            // No hay reserva confirmada: solo se reembolsa y se cancela
            return compensate(order, false, "Inventario no disponible: " + reservation.message());
        }
        // El estado se guarda ANTES de pedir el envío, para que la respuesta asíncrona lo encuentre listo
        order = orderService.updateStatus(orderId, OrderStatus.STOCK_RESERVED, null);
        sagaEvents.publish(orderId, "STOCK_RESERVED", null);

        // Paso 3: envío (asíncrono por Kafka; el resultado llega en onShippingReply)
        try {
            shippingPublisher.sendSchedule(order);
            sagaEvents.publish(orderId, "SHIPPING_REQUESTED", null);
        } catch (Exception ex) {
            return compensate(order, true, "No se pudo solicitar el envío: " + ex.getMessage());
        }

        return orderService.getOrder(orderId);
    }


    // Paso asíncrono: respuesta de Shipping

    public void onShippingReply(ShippingReply reply) {
        OrderResponse order;
        try {
            order = orderService.getOrder(reply.orderId());
        } catch (OrderNotFoundException ex) {
            log.warn("Respuesta de envío para una orden desconocida: {}", reply.orderId());
            return;
        }

        // Idempotencia: solo se actúa si la orden sigue esperando el envío
        if (order.status() != OrderStatus.STOCK_RESERVED) {
            log.info("Respuesta de envío ignorada: la orden {} ya está en {}", order.id(), order.status());
            return;
        }

        switch (reply.status()) {
            case SCHEDULED -> {
                orderService.markCompleted(order.id(), reply.shippingId());
                sagaEvents.publish(order.id(), "ORDER_COMPLETED", null);
            }
            case FAILED -> {
                sagaEvents.publish(order.id(), "SHIPPING_FAILED", reply.reason());
                compensate(order, true, "Envío fallido: " + reply.reason());
            }
            case CANCELLED -> log.info("Envío cancelado para la orden {}", order.id());
        }
    }

    //
    // Compensaciones (en orden inverso): liberar stock → reembolsar → cancelar
    //
    private OrderResponse compensate(OrderResponse order, boolean releaseStock, String reason) {
        UUID orderId = order.id();
        UUID paymentId = order.paymentId();
        sagaEvents.publish(orderId, "COMPENSATION_STARTED", reason);

        boolean released = !releaseStock
                || retry("liberar stock", () -> inventoryClient.release(orderId));
        if (releaseStock && released) {
            sagaEvents.publish(orderId, "STOCK_RELEASED", null);
        }

        boolean refunded = paymentId == null
                || retry("reembolsar pago", () -> paymentClient.refund(paymentId));
        if (paymentId != null && refunded) {
            sagaEvents.publish(orderId, "PAYMENT_REFUNDED", null);
        }

        if (released && refunded) {
            return cancel(orderId, reason);
        }

        log.error("Compensación incompleta para la orden {}: requiere intervención manual", orderId);
        sagaEvents.publish(orderId, "COMPENSATION_FAILED", reason);
        return orderService.updateStatus(orderId, OrderStatus.COMPENSATION_FAILED,
                truncate(reason + " (compensación incompleta)"));
    }

    private OrderResponse cancel(UUID orderId, String reason) {
        OrderResponse cancelled = orderService.updateStatus(orderId, OrderStatus.CANCELLED, truncate(reason));
        sagaEvents.publish(orderId, "ORDER_CANCELLED", reason);
        return cancelled;
    }

    private boolean retry(String action, Runnable task) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                task.run();
                return true;
            } catch (Exception ex) {
                log.warn("Compensación '{}' falló (intento {}/{}): {}", action, attempt, MAX_ATTEMPTS, ex.getMessage());
                try {
                    Thread.sleep(500L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }
}