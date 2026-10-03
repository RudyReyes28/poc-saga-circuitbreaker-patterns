package com.alessandro.payment_service.controller;

import com.alessandro.payment_service.config.FailureSimulator;
import com.alessandro.payment_service.dto.CreatePaymentRequest;
import com.alessandro.payment_service.dto.PaymentResponse;
import com.alessandro.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final FailureSimulator failureSimulator;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        failureSimulator.apply();
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    @PostMapping("/{id}/refund")
    public PaymentResponse refund(@PathVariable UUID id) {
        return paymentService.refund(id);
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable UUID id) {
        return paymentService.getPayment(id);
    }
}