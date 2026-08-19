package com.flowforge.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flowforge.order.entity.Order;

public interface OrderRepository extends JpaRepository <Order, UUID> {

    
}
