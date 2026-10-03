package com.alessandro.shipping_service.service;

import com.alessandro.shipping_service.dto.ScheduleShippingRequest;
import com.alessandro.shipping_service.dto.ShipmentResponse;

import java.util.UUID;

public interface ShippingService {

    ShipmentResponse schedule(ScheduleShippingRequest request);

    ShipmentResponse cancel(UUID shipmentId);

    ShipmentResponse cancelByOrderId(UUID orderId);

    ShipmentResponse getShipment(UUID shipmentId);
}