package com.alessandro.order_service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class ResilienceConfiguration {

    public static final String PAYMENT_CB = "paymentCB";
    public static final String INVENTORY_CB = "inventoryCB";

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> circuitBreakerCustomizer(CircuitBreakerMonitor monitor) {

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)                                   // práctica: 50%
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)                                      // práctica: 10
                .minimumNumberOfCalls(5)                                    // evalúa desde la 5ª llamada
                .waitDurationInOpenState(Duration.ofSeconds(10))            // práctica: 10s
                .permittedNumberOfCallsInHalfOpenState(3)                   // práctica: 3
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(2))                     // llamada > 2s = fallo
                .build();

        return factory -> {
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(circuitBreakerConfig)
                    .timeLimiterConfig(timeLimiterConfig)
                    .build());

            // Log de transiciones + registro para el endpoint de monitoreo
            factory.addCircuitBreakerCustomizer(circuitBreaker -> {
                monitor.register(circuitBreaker);
                circuitBreaker.getEventPublisher().onStateTransition(event ->
                        log.warn("[CB:{}] transición de estado: {}", circuitBreaker.getName(), event.getStateTransition()));
            }, PAYMENT_CB, INVENTORY_CB);
        };
    }
}