package com.flowforge.order.mapper;

import java.math.BigDecimal;
import java.util.List;

import com.flowforge.order.dto.response.OrderItemResponse;
import com.flowforge.order.dto.response.OrderResponse;
import com.flowforge.order.entity.Order;
import com.flowforge.order.entity.OrderItem;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse (Order order, List<OrderItem> items) {

        List<OrderItemResponse> itemResponses = items.stream()
                .map(OrderMapper::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt() // inherited from BaseEntity
        );

    }

    private static OrderItemResponse toItemResponse (OrderItem item) {

        BigDecimal lineTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemResponse(
                item.getItemId(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal
        );

    }
    
}
