package com.flowforge.order.idempotency;

public enum IdempotencyResultType {
    
    NEW,
    IN_PROGRESS,
    COMPLETED,
    REQUEST_MISMATCH

}
