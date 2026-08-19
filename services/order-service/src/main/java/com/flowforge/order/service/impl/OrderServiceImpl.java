package com.flowforge.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowforge.order.client.ItemClient;
import com.flowforge.order.entity.Order;
import com.flowforge.order.entity.OrderItem;
import com.flowforge.order.enums.OrderStatus;
import com.flowforge.order.event.OrderCreatedEvent;
import com.flowforge.order.outbox.OutboxService;
import com.flowforge.order.repository.OrderItemRepository;
import com.flowforge.order.repository.OrderRepository;
import com.flowforge.order.service.CreateOrderCommand;
import com.flowforge.order.service.CreateOrderItemCommand;
import com.flowforge.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemClient itemClient;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public Order createOrder (CreateOrderCommand command) {

        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = Order.builder()
                .customerId(command.customerId())
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderItemCommand itemCommand : command.items()) {

             ItemClient.ItemDetails item =
                    itemClient.getItem(itemCommand.itemId());

            BigDecimal itemTotal = item.unitPrice()
                    .multiply(
                            BigDecimal.valueOf(itemCommand.quantity())
                    );

            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .itemId(item.itemId())
                    .quantity(itemCommand.quantity())
                    .unitPrice(item.unitPrice())
                    .build();

            orderItems.add(orderItem);

        }

        orderItemRepository.saveAll(orderItems);

        order.updateTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        outboxService.record(
                "ORDER",
                savedOrder.getId(),
                "OrderCreated",
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        savedOrder.getCustomerId(),
                        savedOrder.getTotalAmount(),
                        orderItems.stream()
                                .map(oi -> new OrderCreatedEvent.Item(
                                        oi.getItemId(),
                                        oi.getQuantity(),
                                        oi.getUnitPrice()
                                ))
                                .toList(),
                        savedOrder.getCreatedAt()
                )
        );
 
        return savedOrder;

    }
    
}
