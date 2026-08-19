package com.flowforge.order.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest (
    @NotNull(message = "itemId is required")
    UUID itemId,

    @Positive(message = "quantity must be greater than zero")
    int quantity
) {

    
    
}
