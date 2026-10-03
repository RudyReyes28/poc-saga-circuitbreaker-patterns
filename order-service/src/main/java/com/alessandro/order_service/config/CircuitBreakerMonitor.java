package com.alessandro.order_service.config;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CircuitBreakerMonitor {

    private final Map<String, CircuitBreaker> circuits = new ConcurrentHashMap<>();

    public void register(CircuitBreaker circuitBreaker) {
        circuits.put(circuitBreaker.getName(), circuitBreaker);
    }

    public Map<String, Map<String, Object>> snapshot() {
        Map<String, Map<String, Object>> result = new TreeMap<>();
        circuits.forEach((name, cb) -> {
            CircuitBreaker.Metrics metrics = cb.getMetrics();
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("state", cb.getState().name());
            info.put("failureRate", metrics.getFailureRate());   // -1 si aún hay pocas llamadas
            info.put("bufferedCalls", metrics.getNumberOfBufferedCalls());
            info.put("failedCalls", metrics.getNumberOfFailedCalls());
            info.put("notPermittedCalls", metrics.getNumberOfNotPermittedCalls());
            result.put(name, info);
        });
        return result;
    }
}