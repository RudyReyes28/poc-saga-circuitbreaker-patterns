package com.alessandro.order_service.messaging;

import com.alessandro.order_service.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class ShippingCommandPublisher {

    private final KafkaTemplate<String, ShippingCommand> kafkaTemplate;

    public void sendSchedule(OrderResponse order) {
        ShippingCommand command = new ShippingCommand(
                ShippingCommand.Action.SCHEDULE, order.id(),
                order.customerId(), order.productId(), order.quantity());
        try {
            kafkaTemplate.send(Topics.SHIPPING_COMMANDS, order.id().toString(), command)
                    .get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación interrumpida", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Kafka no aceptó el comando de envío", e);
        }
    }
}