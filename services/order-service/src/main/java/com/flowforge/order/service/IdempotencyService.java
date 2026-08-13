package com.flowforge.order.service;

import java.util.UUID;

import com.flowforge.order.idempotency.IdempotencyResult;

public interface IdempotencyService {

    IdempotencyResult checkAndStart (
            UUID customerId,
            String key,
            String requestHash
    );

    void complete (
            UUID customerId,
            String key,
            int responseStatus,
            String responseBody
    );

    void fail (
            UUID customerId,
            String key
    );
    
}
