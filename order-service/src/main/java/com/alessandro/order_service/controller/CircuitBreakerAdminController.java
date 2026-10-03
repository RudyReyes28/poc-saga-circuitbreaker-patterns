package com.alessandro.order_service.controller;

import com.alessandro.order_service.config.CircuitBreakerMonitor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/circuit-breakers")
@RequiredArgsConstructor
public class CircuitBreakerAdminController {

    private final CircuitBreakerMonitor monitor;

    @GetMapping
    public Map<String, Map<String, Object>> status() {
        return monitor.snapshot();
    }
}