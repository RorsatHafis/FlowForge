package com.flowforge.order.idempotency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class IdempotencyResult {

    private final IdempotencyResultType type;
    private final Integer responseStatus;
    private final String responseBody;
    
}