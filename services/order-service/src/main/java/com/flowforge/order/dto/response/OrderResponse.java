package com.flowforge.order.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.flowforge.order.enums.OrderStatus;

public record OrderResponse (
    UUID id,
    UUID customerId,
    OrderStatus status,
    BigDecimal totalAmount,
    List<OrderItemResponse> items,
    Instant createdAt
) {
    
}
