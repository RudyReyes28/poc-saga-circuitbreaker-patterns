package com.alessandro.payment_service.exception;

import java.math.BigDecimal;

public class PaymentRejectedException extends RuntimeException {
    public PaymentRejectedException(BigDecimal amount) {
        super("Pago rechazado: el monto " + amount + " excede el límite permitido");
    }
}