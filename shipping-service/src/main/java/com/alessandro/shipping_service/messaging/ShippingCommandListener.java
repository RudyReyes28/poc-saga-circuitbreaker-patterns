package com.alessandro.shipping_service.messaging;

import com.alessandro.shipping_service.config.FailureSimulator;
import com.alessandro.shipping_service.dto.ScheduleShippingRequest;
import com.alessandro.shipping_service.dto.ShipmentResponse;
import com.alessandro.shipping_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingCommandListener {

    public static final String COMMANDS_TOPIC = "shipping.commands";
    public static final String REPLIES_TOPIC = "shipping.replies";

    private final ShippingService shippingService;
    private final FailureSimulator failureSimulator;
    private final KafkaTemplate<String, ShippingReply> kafkaTemplate;

    @KafkaListener(topics = COMMANDS_TOPIC)
    public void onCommand(ShippingCommand command) {
        log.info("Comando recibido: {} para la orden {}", command.action(), command.orderId());

        ShippingReply reply;
        try {
            failureSimulator.apply();

            reply = switch (command.action()) {
                case SCHEDULE -> {
                    ShipmentResponse shipment = shippingService.schedule(new ScheduleShippingRequest(
                            command.orderId(), command.customerId(),
                            command.productId(), command.quantity()));
                    yield new ShippingReply(command.orderId(), shipment.id(),
                            ShippingReply.Status.SCHEDULED, null);
                }
                case CANCEL -> {
                    ShipmentResponse shipment = shippingService.cancelByOrderId(command.orderId());
                    yield new ShippingReply(command.orderId(), shipment.id(),
                            ShippingReply.Status.CANCELLED, null);
                }
            };
        } catch (Exception ex) {
            // Siempre se responde, incluso ante fallos: así el orquestador puede compensar
            log.warn("Fallo procesando comando para la orden {}: {}", command.orderId(), ex.getMessage());
            reply = new ShippingReply(command.orderId(), null,
                    ShippingReply.Status.FAILED, ex.getMessage());
        }

        kafkaTemplate.send(REPLIES_TOPIC, command.orderId().toString(), reply);
    }
}