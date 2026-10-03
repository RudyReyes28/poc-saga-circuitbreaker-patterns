package com.alessandro.inventory_service.repository;

import com.alessandro.inventory_service.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {

    @Modifying(clearAutomatically = true)
    @Query("""
            update InventoryItem i
               set i.availableQuantity = i.availableQuantity - :qty
             where i.productId = :productId
               and i.availableQuantity >= :qty
            """)
    int decreaseStock(@Param("productId") String productId, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("""
            update InventoryItem i
               set i.availableQuantity = i.availableQuantity + :qty
             where i.productId = :productId
            """)
    int increaseStock(@Param("productId") String productId, @Param("qty") int qty);
}