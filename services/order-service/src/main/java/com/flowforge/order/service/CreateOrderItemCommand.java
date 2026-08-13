package com.flowforge.order.service;

import java.util.UUID;

public record CreateOrderItemCommand (
    UUID itemId,
    int quantity
) {
    
}
