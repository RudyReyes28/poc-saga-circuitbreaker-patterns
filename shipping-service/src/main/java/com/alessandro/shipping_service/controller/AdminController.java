package com.alessandro.shipping_service.controller;

import com.alessandro.shipping_service.config.FailureSimulator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/fail-mode")
@RequiredArgsConstructor
public class AdminController {

    private final FailureSimulator simulator;

    public record FailModeRequest(FailureSimulator.Mode mode, Long delayMs) {}

    @PostMapping
    public Map<String, Object> set(@RequestBody FailModeRequest request) {
        simulator.configure(request.mode(), request.delayMs());
        return current();
    }

    @GetMapping
    public Map<String, Object> current() {
        return Map.of("mode", simulator.getMode(), "delayMs", simulator.getDelayMs());
    }
}