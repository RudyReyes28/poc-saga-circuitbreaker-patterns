package com.alessandro.order_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventPublisher {

    private final KafkaTemplate<String, SagaEvent> kafkaTemplate;
    private final SagaEventStore eventStore;

    public void publish(UUID orderId, String event, String detail) {
        SagaEvent sagaEvent = new SagaEvent(orderId, event, detail, Instant.now());

        // Copia local para el panel de demostración
        eventStore.add(sagaEvent);

        // Publicación en Kafka: mejor esfuerzo, un fallo aquí no interrumpe la saga
        try {
            kafkaTemplate.send(Topics.SAGA_EVENTS, orderId.toString(), sagaEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("No se pudo publicar el evento {}: {}", event, ex.getMessage());
                        }
                    });
        } catch (Exception ex) {
            log.warn("No se pudo publicar el evento {}: {}", event, ex.getMessage());
        }
    }
}