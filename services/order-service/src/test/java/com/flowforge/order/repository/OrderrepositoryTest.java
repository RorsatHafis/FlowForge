package com.flowforge.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import com.flowforge.order.config.JpaConfig;
import com.flowforge.order.entity.Order;
import com.flowforge.order.enums.OrderStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;
 
    @Test
    void shouldPersistOrderWithUuidV7AndAuditFields() {
 
        Order order = Order.builder()
                .customerId(UUID.randomUUID())
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("99.99"))
                .build();
 
        Order saved = orderRepository.saveAndFlush(order);
 
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
 
        assertThat(saved.getId().version()).isEqualTo(7);
        assertThat(saved.getId().variant()).isEqualTo(2);
 
        Order found = orderRepository.findById(saved.getId())
                .orElseThrow();
 
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getCustomerId()).isEqualTo(order.getCustomerId());
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(found.getTotalAmount()).isEqualByComparingTo("99.99");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();

    }
    
}
