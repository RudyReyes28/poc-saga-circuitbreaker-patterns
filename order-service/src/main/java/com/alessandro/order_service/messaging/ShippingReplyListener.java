package com.alessandro.order_service.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShippingReplyListener {

    private final OrderSagaOrchestrator orchestrator;

    @KafkaListener(topics = Topics.SHIPPING_REPLIES)
    public void onReply(ShippingReply reply) {
        orchestrator.onShippingReply(reply);
    }
}