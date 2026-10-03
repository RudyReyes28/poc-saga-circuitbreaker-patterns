package com.alessandro.inventory_service.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem {

    @Id
    @Column(name = "product_id", length = 100)
    private String productId;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
}