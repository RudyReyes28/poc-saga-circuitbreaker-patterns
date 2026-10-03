package com.alessandro.order_service.controller;


import com.alessandro.order_service.messaging.SagaEventStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints de apoyo SOLO para el panel de demostración de la PoC.
 * Hacen de intermediario hacia los otros servicios, así el navegador habla únicamente
 * con el Order Service y no hace falta configurar CORS.
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    public record EventView(String event, String detail, long at) {}

    public record FailModeRequest(String mode, Long delayMs) {}

    public record StockData(String productId, Integer availableQuantity) {}

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final List<String> PRODUCTS = List.of("prod-001", "prod-002", "prod-003");

    private final SagaEventStore eventStore;
    private final Map<String, RestClient> services = new LinkedHashMap<>();

    public DemoController(SagaEventStore eventStore,
                          @Value("${services.payment.url}") String paymentUrl,
                          @Value("${services.inventory.url}") String inventoryUrl,
                          @Value("${services.shipping.url}") String shippingUrl) {
        this.eventStore = eventStore;
        this.services.put("payment", client(paymentUrl));
        this.services.put("inventory", client(inventoryUrl));
        this.services.put("shipping", client(shippingUrl));
    }

    /** Cliente con timeouts cortos, para que el panel no se cuelgue si un servicio está caído. */
    private static RestClient client(String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(2));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @GetMapping("/events")
    public List<EventView> events(@RequestParam UUID orderId) {
        return eventStore.findByOrderId(orderId).stream()
                .map(e -> new EventView(e.event(), e.detail(), e.occurredAt().toEpochMilli()))
                .toList();
    }

    @GetMapping("/fail-mode")
    public Map<String, Map<String, Object>> failModes() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        services.forEach((name, client) -> {
            try {
                result.put(name, client.get().uri("/admin/fail-mode").retrieve().body(MAP_TYPE));
            } catch (RestClientException ex) {
                result.put(name, Map.of("mode", "UNREACHABLE"));
            }
        });
        return result;
    }

    @PostMapping("/fail-mode/{service}")
    public Map<String, Object> setFailMode(@PathVariable String service,
                                           @RequestBody FailModeRequest request) {
        RestClient client = services.get(service);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio desconocido: " + service);
        }
        try {
            return client.post()
                    .uri("/admin/fail-mode")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MAP_TYPE);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo contactar al servicio " + service);
        }
    }

    @GetMapping("/stock")
    public Map<String, Integer> stock() {
        RestClient inventory = services.get("inventory");
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String productId : PRODUCTS) {
            try {
                StockData data = inventory.get()
                        .uri("/inventory/{id}", productId)
                        .retrieve()
                        .body(StockData.class);
                result.put(productId, data == null ? null : data.availableQuantity());
            } catch (RestClientException ex) {
                result.put(productId, null);
            }
        }
        return result;
    }
}