package com.alessandro.shipping_service.service;

import com.alessandro.shipping_service.dto.ScheduleShippingRequest;
import com.alessandro.shipping_service.dto.ShipmentResponse;
import com.alessandro.shipping_service.entity.Shipment;
import com.alessandro.shipping_service.enums.ShippingStatus;
import com.alessandro.shipping_service.exception.ShipmentNotFoundException;
import com.alessandro.shipping_service.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShippingServiceImpl implements ShippingService {

    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional
    public ShipmentResponse schedule(ScheduleShippingRequest request) {
        // Idempotencia: un comando duplicado devuelve el mismo envío
        return shipmentRepository.findByOrderId(request.orderId())
                .map(ShipmentResponse::from)
                .orElseGet(() -> createShipment(request));
    }

    @Override
    @Transactional
    public ShipmentResponse cancel(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Envío no encontrado: " + shipmentId));
        return cancelIfNeeded(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse cancelByOrderId(UUID orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException("No existe envío para la orden: " + orderId));
        return cancelIfNeeded(shipment);
    }

    @Override
    public ShipmentResponse getShipment(UUID shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .map(ShipmentResponse::from)
                .orElseThrow(() -> new ShipmentNotFoundException("Envío no encontrado: " + shipmentId));
    }

    private ShipmentResponse createShipment(ScheduleShippingRequest request) {
        Shipment shipment = new Shipment();
        shipment.setOrderId(request.orderId());
        shipment.setCustomerId(request.customerId());
        shipment.setProductId(request.productId());
        shipment.setQuantity(request.quantity());
        shipment.setTrackingCode("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        shipment.setStatus(ShippingStatus.SCHEDULED);
        return ShipmentResponse.from(shipmentRepository.save(shipment));
    }

    private ShipmentResponse cancelIfNeeded(Shipment shipment) {
        // Idempotencia: cancelar dos veces no cambia nada
        if (shipment.getStatus() != ShippingStatus.CANCELLED) {
            shipment.setStatus(ShippingStatus.CANCELLED);
        }
        return ShipmentResponse.from(shipment);
    }
}