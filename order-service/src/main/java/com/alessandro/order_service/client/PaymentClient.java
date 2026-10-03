package com.alessandro.order_service.client;

import com.alessandro.order_service.config.ResilienceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class PaymentClient {

    public record ChargeRequest(UUID orderId, String customerId, BigDecimal amount) {}

    public record PaymentData(UUID id, UUID orderId, String status) {}

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public PaymentClient(CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                         @Value("${services.payment.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create(ResilienceConfiguration.PAYMENT_CB);
    }

    /** Protegido por Circuit Breaker #1. */
    public StepResult<PaymentData> charge(ChargeRequest request) {
        return circuitBreaker.run(
                () -> doCharge(request),
                throwable -> {
                    log.warn("Fallback de pagos activado: {}", throwable.getClass().getSimpleName());
                    return StepResult.fromFailure("pagos", throwable);
                });
    }

    /** Compensación: sin Circuit Breaker, debe intentarse siempre (con reintentos en el orquestador). */
    public void refund(UUID paymentId) {
        restClient.post()
                .uri("/payments/{id}/refund", paymentId)
                .retrieve()
                .toBodilessEntity();
    }

    private StepResult<PaymentData> doCharge(ChargeRequest request) {
        try {
            PaymentData data = restClient.post()
                    .uri("/payments")
                    .body(request)
                    .retrieve()
                    .body(PaymentData.class);
            return StepResult.success(data);
        } catch (HttpClientErrorException ex) {
            return StepResult.fromHttpError(ex);   // 4xx: se devuelve, no se lanza
        }
        // Un 5xx, timeout o conexión rechazada SÍ se propaga: cuenta como fallo del circuito
    }
}