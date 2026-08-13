package com.flowforge.order.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flowforge.order.client.ItemClient;
import com.flowforge.order.entity.Order;
import com.flowforge.order.enums.OrderStatus;
import com.flowforge.order.repository.OrderItemRepository;
import com.flowforge.order.repository.OrderRepository;
import com.flowforge.order.service.CreateOrderCommand;
import com.flowforge.order.service.CreateOrderItemCommand;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ItemClient itemClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID customerId;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        itemId = UUID.randomUUID();
    }

    @Test
    void shouldCreateOrderAndCalculateTotal() {

        CreateOrderCommand command = new CreateOrderCommand(
                customerId,
                List.of(
                        new CreateOrderItemCommand(
                                itemId,
                                2
                        )
                )
        );

        when(itemClient.getItem(itemId))
                .thenReturn(
                        new ItemClient.ItemDetails(
                                itemId,
                                new BigDecimal("10.00")
                        )
                );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(command);

        assertNotNull(result);

        assertEquals(
                customerId,
                result.getCustomerId()
        );

        assertEquals(
                OrderStatus.PENDING_PAYMENT,
                result.getStatus()
        );

        assertEquals(
                new BigDecimal("20.00"),
                result.getTotalAmount()
        );

        verify(itemClient)
                .getItem(itemId);

        verify(orderRepository, times(2))
                .save(any(Order.class));

        verify(orderItemRepository)
                .saveAll(anyList());

    }

}