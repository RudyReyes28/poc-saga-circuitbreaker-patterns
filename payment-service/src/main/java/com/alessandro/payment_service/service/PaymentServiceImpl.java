package com.alessandro.payment_service.service;

import com.alessandro.payment_service.anums.PaymentStatus;
import com.alessandro.payment_service.dto.CreatePaymentRequest;
import com.alessandro.payment_service.dto.PaymentResponse;
import com.alessandro.payment_service.entity.Payment;
import com.alessandro.payment_service.exception.PaymentNotFoundException;
import com.alessandro.payment_service.exception.PaymentRejectedException;
import com.alessandro.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    // Regla de negocio simple para poder probar un rechazo de pago en la saga
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponse processPayment(CreatePaymentRequest request) {
        // Idempotencia: si ya existe un pago para esta orden, se devuelve el mismo
        return paymentRepository.findByOrderId(request.orderId())
                .map(PaymentResponse::from)
                .orElseGet(() -> createApprovedPayment(request));
    }

    @Override
    @Transactional
    public PaymentResponse refund(UUID paymentId) {
        Payment payment = findOrThrow(paymentId);
        // Idempotencia: reembolsar dos veces no cambia nada
        if (payment.getStatus() != PaymentStatus.REFUNDED) {
            payment.setStatus(PaymentStatus.REFUNDED);
        }
        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse getPayment(UUID paymentId) {
        return PaymentResponse.from(findOrThrow(paymentId));
    }

    private PaymentResponse createApprovedPayment(CreatePaymentRequest request) {
        if (request.amount().compareTo(MAX_AMOUNT) > 0) {
            throw new PaymentRejectedException(request.amount());
        }
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setCustomerId(request.customerId());
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.APPROVED);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    private Payment findOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }
}