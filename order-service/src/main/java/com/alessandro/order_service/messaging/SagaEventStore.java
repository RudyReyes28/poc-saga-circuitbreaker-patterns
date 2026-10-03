package com.alessandro.order_service.messaging;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Guarda en memoria una copia de los eventos de la saga (los mismos que se publican en Kafka)
 * para que el panel de demostración pueda mostrarlos. Se pierden al reiniciar el servicio.
 */
@Component
public class SagaEventStore {

    private static final int MAX_EVENTS = 1000;

    private final ConcurrentLinkedDeque<SagaEvent> events = new ConcurrentLinkedDeque<>();

    public void add(SagaEvent event) {
        events.addLast(event);
        while (events.size() > MAX_EVENTS) {
            events.pollFirst();
        }
    }

    public List<SagaEvent> findByOrderId(UUID orderId) {
        return events.stream()
                .filter(event -> event.orderId().equals(orderId))
                .toList();
    }
}
