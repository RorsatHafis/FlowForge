package com.flowforge.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flowforge.order.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository <OrderItem, UUID> {
    
}
