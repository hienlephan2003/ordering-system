package org.ordering.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.ordering.domain.valueobject.OrderId;
import org.ordering.domain.valueobject.OrderStatus;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.exception.OrderNotFoundException;
import org.ordering.order.service.domain.repository.OrderRepository;
import org.ordering.saga.SagaStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
@Slf4j
@Component
public class OrderSagaHelper {
    private final OrderRepository orderRepository;

    public OrderSagaHelper(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    Order findOrder(UUID orderId) {
        Optional<Order> orderResponse = orderRepository.findById(new OrderId((orderId)));
        if (orderResponse.isEmpty()) {
            log.error("Order with id: {} could not be found!", orderId);
            throw new OrderNotFoundException("Order with id " + orderId + " could not be found!");
        }
        return orderResponse.get();
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
