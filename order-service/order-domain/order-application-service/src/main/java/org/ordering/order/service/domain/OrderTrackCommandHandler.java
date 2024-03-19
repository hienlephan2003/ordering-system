package org.ordering.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.ordering.order.service.domain.dto.create.CreateOrderCommand;
import org.ordering.order.service.domain.dto.create.CreateOrderResponse;
import org.ordering.order.service.domain.dto.track.TrackOrderQuery;
import org.ordering.order.service.domain.dto.track.TrackOrderResponse;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.exception.OrderNotFoundException;
import org.ordering.order.service.domain.mapper.OrderDataMapper;
import org.ordering.order.service.domain.repository.OrderRepository;
import org.ordering.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Slf4j
@Component
public class OrderTrackCommandHandler  {
    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    public OrderTrackCommandHandler(OrderDataMapper orderDataMapper, OrderRepository orderRepository) {
        this.orderDataMapper = orderDataMapper;
        this.orderRepository = orderRepository;
    }

    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery){
        Optional<Order> order = orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));
        if (order.isEmpty()) {
            log.warn("Could not find order with tracking id: {}", trackOrderQuery.getOrderTrackingId());
            throw new OrderNotFoundException("Could not find order with tracking id: " +
                    trackOrderQuery.getOrderTrackingId());
        }

        return orderDataMapper.orderToTrackOrderResponse(order.get());
    }
}
