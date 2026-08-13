package com.flowforge.order.service;


import com.flowforge.order.entity.Order;

public interface OrderService {

    Order createOrder(CreateOrderCommand command);
    
}
