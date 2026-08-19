package com.flowforge.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        List<Item> items,
        Instant createdAt
) {
 
    public record Item (UUID itemId, int quantity, BigDecimal unitPrice) {}
 
}