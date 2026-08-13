package com.flowforge.order.entity;


import java.time.Instant;
import java.util.UUID;

import com.flowforge.order.entity.base.BaseEntity;
import com.flowforge.order.enums.IdempotencyStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "idempotency_keys",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_idempotency_keys_customer_key",
            columnNames = {"customer_id", "key"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyKey extends BaseEntity {

    @Column(name = "key", nullable = false, length = 255)
    private String key;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IdempotencyStatus status;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
}
