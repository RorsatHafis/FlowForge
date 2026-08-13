package com.flowforge.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flowforge.order.entity.IdempotencyKey;

public interface IdempotencyKeyRepository extends JpaRepository <IdempotencyKey, UUID> {

    Optional<IdempotencyKey> findByCustomerIdAndKey(
            UUID customerId,
            String key
    );
    
}
