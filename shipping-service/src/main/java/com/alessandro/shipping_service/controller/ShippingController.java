package com.alessandro.shipping_service.controller;

import com.alessandro.shipping_service.config.FailureSimulator;
import com.alessandro.shipping_service.dto.ScheduleShippingRequest;
import com.alessandro.shipping_service.dto.ShipmentResponse;
import com.alessandro.shipping_service.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;
    private final FailureSimulator failureSimulator;

    @PostMapping("/schedule")
    public ResponseEntity<ShipmentResponse> schedule(@Valid @RequestBody ScheduleShippingRequest request) {
        failureSimulator.apply();
        return ResponseEntity.status(HttpStatus.CREATED).body(shippingService.schedule(request));
    }

    @DeleteMapping("/{id}")
    public ShipmentResponse cancel(@PathVariable UUID id) {
        return shippingService.cancel(id);
    }

    @GetMapping("/{id}")
    public ShipmentResponse getById(@PathVariable UUID id) {
        return shippingService.getShipment(id);
    }
}