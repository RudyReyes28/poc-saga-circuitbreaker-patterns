package com.alessandro.order_service.client;

import com.alessandro.order_service.config.ResilienceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Component
public class InventoryClient {

    public record ReserveRequest(UUID orderId, String productId, Integer quantity) {}

    public record ReleaseRequest(UUID orderId) {}

    public record ReservationData(UUID id, UUID orderId, String status) {}

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public InventoryClient(CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                           @Value("${services.inventory.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create(ResilienceConfiguration.INVENTORY_CB);
    }

    /** Protegido por Circuit Breaker #2. */
    public StepResult<ReservationData> reserve(ReserveRequest request) {
        return circuitBreaker.run(
                () -> doReserve(request),
                throwable -> {
                    log.warn("Fallback de inventario activado: {}", throwable.getClass().getSimpleName());
                    return StepResult.fromFailure("inventario", throwable);
                });
    }

    /** Compensación: sin Circuit Breaker. Si no había reserva (404), no hay nada que liberar. */
    public void release(UUID orderId) {
        try {
            restClient.post()
                    .uri("/inventory/release")
                    .body(new ReleaseRequest(orderId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ex) {
            log.info("No existía reserva para la orden {}: nada que liberar", orderId);
        }
    }

    private StepResult<ReservationData> doReserve(ReserveRequest request) {
        try {
            ReservationData data = restClient.post()
                    .uri("/inventory/reserve")
                    .body(request)
                    .retrieve()
                    .body(ReservationData.class);
            return StepResult.success(data);
        } catch (HttpClientErrorException ex) {
            return StepResult.fromHttpError(ex);   // 409 sin stock, 404 producto: negocio
        }
    }
}