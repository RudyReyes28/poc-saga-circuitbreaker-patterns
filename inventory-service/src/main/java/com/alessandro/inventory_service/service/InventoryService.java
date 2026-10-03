package com.alessandro.inventory_service.service;

import com.alessandro.inventory_service.dto.ReleaseStockRequest;
import com.alessandro.inventory_service.dto.ReservationResponse;
import com.alessandro.inventory_service.dto.ReserveStockRequest;
import com.alessandro.inventory_service.dto.StockResponse;

public interface InventoryService {

    ReservationResponse reserve(ReserveStockRequest request);

    ReservationResponse release(ReleaseStockRequest request);

    StockResponse getStock(String productId);
}