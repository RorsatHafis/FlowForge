package com.flowforge.order.service;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand (
    UUID customerId,
    List<CreateOrderItemCommand> items
) {

    
}
