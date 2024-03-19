package org.ordering.order.service.domain;

import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.entity.Restaurant;
import org.ordering.order.service.domain.event.OrderCancelledEvent;
import org.ordering.order.service.domain.event.OrderCreatedEvent;
import org.ordering.order.service.domain.event.OrderPaidEvent;

import java.util.List;

public interface OrderDomainService {
    OrderCreatedEvent validateAndInitializeOrder(Order order, Restaurant restaurant);
    OrderPaidEvent payOrder(Order order);
    void approveOrder(Order order);
    OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages);
    void cancelOrder(Order order, List<String> failureMessages);
}
