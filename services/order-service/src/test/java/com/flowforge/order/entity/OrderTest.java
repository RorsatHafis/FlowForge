package com.flowforge.order.entity;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
 
import java.math.BigDecimal;
import java.util.UUID;
 
import org.junit.jupiter.api.Test;
 
import com.flowforge.order.enums.OrderStatus;
import com.flowforge.order.exception.InvalidOrderStateException;
 
class OrderTest {
 
    private Order orderWithStatus(OrderStatus status) {
 
        return Order.builder()
                .customerId(UUID.randomUUID())
                .status(status)
                .totalAmount(BigDecimal.TEN)
                .build();
 
    }
 
    @Test
    void shouldAllowPendingPaymentToConfirmed() {
 
        Order order = orderWithStatus(OrderStatus.PENDING_PAYMENT);
 
        order.transitionTo(OrderStatus.CONFIRMED);
 
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
 
    }
 
    @Test
    void shouldAllowPendingPaymentToPaymentReview() {
 
        Order order = orderWithStatus(OrderStatus.PENDING_PAYMENT);
 
        order.transitionTo(OrderStatus.PAYMENT_REVIEW);
 
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_REVIEW);
 
    }
 
    @Test
    void shouldAllowPaymentReviewToConfirmed() {
 
        Order order = orderWithStatus(OrderStatus.PAYMENT_REVIEW);
 
        order.transitionTo(OrderStatus.CONFIRMED);
 
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
 
    }
 
    @Test
    void shouldAllowPaymentReviewToCancelled() {
 
        Order order = orderWithStatus(OrderStatus.PAYMENT_REVIEW);
 
        order.transitionTo(OrderStatus.CANCELLED);
 
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
 
    }
 
    @Test
    void shouldRejectConfirmedToPaymentReview() {
 
        // this is the specific transition called out by name in the
        // FlowForge design doc as something that must never be allowed
        Order order = orderWithStatus(OrderStatus.CONFIRMED);
 
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.PAYMENT_REVIEW))
                .isInstanceOf(InvalidOrderStateException.class);
 
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
 
    }
 
    @Test
    void shouldRejectAnyTransitionOutOfConfirmed() {
 
        Order order = orderWithStatus(OrderStatus.CONFIRMED);
 
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.CANCELLED))
                .isInstanceOf(InvalidOrderStateException.class);
 
    }
 
    @Test
    void shouldRejectAnyTransitionOutOfCancelled() {
 
        Order order = orderWithStatus(OrderStatus.CANCELLED);
 
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.CONFIRMED))
                .isInstanceOf(InvalidOrderStateException.class);
 
    }
 
    @Test
    void shouldTreatSameStatusTransitionAsNoOp() {
 
        // this matters for webhook idempotency later: a duplicate
        // "payment succeeded" event re-confirming an already-CONFIRMED
        // order should not blow up
        Order order = orderWithStatus(OrderStatus.CONFIRMED);
 
        order.transitionTo(OrderStatus.CONFIRMED);
 
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
 
    }
 
}
 