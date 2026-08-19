package com.flowforge.order.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse (
    UUID itemId,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {
    
}
