package com.alessandro.payment_service.service;


import com.alessandro.payment_service.dto.CreatePaymentRequest;
import com.alessandro.payment_service.dto.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse processPayment(CreatePaymentRequest request);

    PaymentResponse refund(UUID paymentId);

    PaymentResponse getPayment(UUID paymentId);
}