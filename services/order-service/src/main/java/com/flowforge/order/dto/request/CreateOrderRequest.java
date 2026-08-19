package com.flowforge.order.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest (
    @NotNull(message = "customerId is required")
    UUID customerId,

    @NotEmpty(message = "order must contain at least one item") 
    @Valid
    List<OrderItemRequest> items
) {
    
}
