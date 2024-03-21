package org.ordering.order.service.domain;

import org.ordering.domain.valueobject.OrderId;
import org.ordering.domain.valueobject.OrderStatus;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.repository.OrderRepository;
import org.ordering.saga.SagaStatus;

import java.util.Optional;
import java.util.UUID;

public class OrderSagaHelper {
    private final OrderRepository orderRepository;

    public OrderSagaHelper(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    Optional<Order> findOrder(String orderId){
        return orderRepository.findById(new OrderId(UUID.fromString(orderId)));
    }
    void saveOrder(Order order){
        orderRepository.save(order);
    }
    SagaStatus orderStatusToSagaStatus(OrderStatus orderStatus) {
        switch (orderStatus){
            case PAID -> {
                return SagaStatus.PROCESSING;
            }
            case APPROVED -> {
                return SagaStatus.SUCCEEDED;
            }
            case CANCELLED -> {
                return SagaStatus.COMPENSATED;
            }
            case CANCELLING -> {
                return SagaStatus.COMPENSATING;
            }
            default -> {
                return SagaStatus.STARTED;
            }
        }
    }
}
