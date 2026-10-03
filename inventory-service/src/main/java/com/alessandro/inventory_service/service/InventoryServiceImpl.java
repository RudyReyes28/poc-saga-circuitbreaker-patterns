package com.alessandro.inventory_service.service;

import com.alessandro.inventory_service.dto.ReleaseStockRequest;
import com.alessandro.inventory_service.dto.ReservationResponse;
import com.alessandro.inventory_service.dto.ReserveStockRequest;
import com.alessandro.inventory_service.dto.StockResponse;
import com.alessandro.inventory_service.entity.InventoryItem;
import com.alessandro.inventory_service.entity.Reservation;
import com.alessandro.inventory_service.enums.ReservationStatus;
import com.alessandro.inventory_service.exception.InsufficientStockException;
import com.alessandro.inventory_service.exception.ProductNotFoundException;
import com.alessandro.inventory_service.exception.ReservationNotFoundException;
import com.alessandro.inventory_service.repository.InventoryItemRepository;
import com.alessandro.inventory_service.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository itemRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public ReservationResponse reserve(ReserveStockRequest request) {
        // Idempotencia: si la orden ya tiene reserva, se devuelve la misma
        return reservationRepository.findByOrderId(request.orderId())
                .map(ReservationResponse::from)
                .orElseGet(() -> createReservation(request));
    }

    @Override
    @Transactional
    public ReservationResponse release(ReleaseStockRequest request) {
        Reservation reservation = reservationRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new ReservationNotFoundException(request.orderId()));

        // Idempotencia: liberar dos veces no devuelve el stock dos veces
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            itemRepository.increaseStock(reservation.getProductId(), reservation.getQuantity());
            reservation.setStatus(ReservationStatus.RELEASED);
        }
        return ReservationResponse.from(reservation);
    }

    @Override
    public StockResponse getStock(String productId) {
        InventoryItem item = itemRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return new StockResponse(item.getProductId(), item.getAvailableQuantity());
    }

    private ReservationResponse createReservation(ReserveStockRequest request) {
        if (!itemRepository.existsById(request.productId())) {
            throw new ProductNotFoundException(request.productId());
        }
        int updated = itemRepository.decreaseStock(request.productId(), request.quantity());
        if (updated == 0) {
            throw new InsufficientStockException(request.productId(), request.quantity());
        }

        Reservation reservation = new Reservation();
        reservation.setOrderId(request.orderId());
        reservation.setProductId(request.productId());
        reservation.setQuantity(request.quantity());
        reservation.setStatus(ReservationStatus.RESERVED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }
}