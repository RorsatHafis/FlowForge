package com.flowforge.order.entity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


import com.flowforge.order.entity.base.BaseEntity;
import com.flowforge.order.enums.OrderStatus;
import com.flowforge.order.exception.InvalidOrderStateException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order extends BaseEntity {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING_PAYMENT, Set.of(
                    OrderStatus.PAYMENT_REVIEW,
                    OrderStatus.CONFIRMED,
                    OrderStatus.CANCELLED
            ),
            OrderStatus.PAYMENT_REVIEW, Set.of(
                    OrderStatus.CONFIRMED,
                    OrderStatus.CANCELLED
            ),
            OrderStatus.CONFIRMED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    public void updateTotalAmount(BigDecimal totalAmount) {

        this.totalAmount = totalAmount;

    }

    public void transitionTo (OrderStatus newStatus) {
 
        if (newStatus == this.status) {
            return;
        }
 
        Set<OrderStatus> allowedNextStates = ALLOWED_TRANSITIONS.getOrDefault(this.status, Set.of());
 
        if (!allowedNextStates.contains(newStatus)) {
            throw new InvalidOrderStateException(
                    "Cannot transition order " + getId() + " from " + this.status + " to " + newStatus
            );
        }
 
        this.status = newStatus;
 
    }
    
}
