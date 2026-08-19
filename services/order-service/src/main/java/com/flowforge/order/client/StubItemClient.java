package com.flowforge.order.client;
import java.math.BigDecimal;
import java.util.UUID;
 
import org.springframework.stereotype.Component;
 
/**
 * Temporary stand-in for the real Inventory Service HTTP client.
 *
 * <p>Order Service depends on {@link ItemClient} to resolve an item's current
 * unit price when placing an order. Inventory Service (:8082) does not exist
 * yet, so there is nothing real to call. This stub satisfies the Spring bean
 * requirement and lets Order Service run and be tested end-to-end in the
 * meantime.
 *
 * <p>TODO: replace with a RestClient/WebClient-based implementation that
 * calls Inventory Service once it exists. Delete this class at that point —
 * {@link com.flowforge.order.service.impl.OrderServiceImpl} depends only on
 * the {@link ItemClient} interface, so nothing else needs to change.
 */
@Component
public class StubItemClient implements ItemClient {
 
    private static final BigDecimal FIXED_UNIT_PRICE = new BigDecimal("10.00");
 
    @Override
    public ItemDetails getItem(UUID itemId) {
        return new ItemDetails(itemId, FIXED_UNIT_PRICE);
    }
 
}