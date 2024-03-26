package org.ordering.order.service.dataaccess.order.adapter;

import org.ordering.domain.valueobject.OrderId;
import org.ordering.order.service.dataaccess.order.mapper.OrderDataAccessMapper;
import org.ordering.order.service.dataaccess.order.repository.OrderJpaRepository;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.repository.OrderRepository;
import org.ordering.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;
    private final OrderDataAccessMapper orderDataAccessMapper;
    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository,
                               OrderDataAccessMapper orderDataAccessMapper) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderDataAccessMapper = orderDataAccessMapper;
    }


    @Override
    public Order save(Order order) {
        return orderDataAccessMapper.orderEntityToOrder( orderJpaRepository.save(orderDataAccessMapper.orderToOrderEntity(order)));
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.getValue()).map(orderDataAccessMapper::orderEntityToOrder);
    }

    @Override
    public Optional<Order> findByTrackingId(TrackingId trackingId) {
        return orderJpaRepository.findOrderEntitiesByTrackingId(trackingId.getValue()).map(orderDataAccessMapper::orderEntityToOrder);
    }
}
