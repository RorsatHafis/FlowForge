package com.flowforge.order.client;

import java.math.BigDecimal;
import java.util.UUID;

public interface ItemClient {

    ItemDetails getItem(UUID itemId);

    record ItemDetails(
            UUID itemId,
            BigDecimal unitPrice
    ) {
    }
    
}
