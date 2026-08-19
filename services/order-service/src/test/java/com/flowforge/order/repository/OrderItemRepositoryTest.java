package com.flowforge.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.flowforge.order.config.JpaConfig;
import com.flowforge.order.entity.Order;
import com.flowforge.order.entity.OrderItem;
import com.flowforge.order.enums.OrderStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class OrderItemRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void shouldPersistOrderItemWithOrderRelationship() {

        Order order = Order.builder()
                .customerId(UUID.randomUUID())
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("199.98"))
                .build();

        Order savedOrder = orderRepository.saveAndFlush(order);

        OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .itemId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .build();

        OrderItem savedItem = orderItemRepository.saveAndFlush(orderItem);

        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getId().version()).isEqualTo(7);
        assertThat(savedItem.getId().variant()).isEqualTo(2);

        assertThat(savedItem.getCreatedAt()).isNotNull();
        assertThat(savedItem.getUpdatedAt()).isNotNull();

        assertThat(savedItem.getOrder()).isNotNull();
        assertThat(savedItem.getOrder().getId())
                .isEqualTo(savedOrder.getId());

        assertThat(savedItem.getQuantity()).isEqualTo(2);
        assertThat(savedItem.getUnitPrice())
                .isEqualByComparingTo("99.99");
    }
}