package org.ordering.order.service.domain.repository;


import org.ordering.domain.valueobject.OrderId;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(OrderId orderId);

    Optional<Order> findByTrackingId(TrackingId trackingId);
}
