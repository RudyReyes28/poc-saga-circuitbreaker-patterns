package com.alessandro.inventory_service.controller;

import com.alessandro.inventory_service.config.FailureSimulator;
import com.alessandro.inventory_service.dto.ReleaseStockRequest;
import com.alessandro.inventory_service.dto.ReservationResponse;
import com.alessandro.inventory_service.dto.ReserveStockRequest;
import com.alessandro.inventory_service.dto.StockResponse;
import com.alessandro.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final FailureSimulator failureSimulator;

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReserveStockRequest request) {
        failureSimulator.apply();
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.reserve(request));
    }

    @PostMapping("/release")
    public ReservationResponse release(@Valid @RequestBody ReleaseStockRequest request) {
        return inventoryService.release(request);
    }

    @GetMapping("/{productId}")
    public StockResponse getStock(@PathVariable String productId) {
        return inventoryService.getStock(productId);
    }
}