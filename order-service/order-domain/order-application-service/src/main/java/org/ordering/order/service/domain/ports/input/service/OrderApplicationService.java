package org.ordering.order.service.domain.ports.input.service;

import jakarta.validation.Valid;
import org.ordering.order.service.domain.dto.create.CreateOrderCommand;
import org.ordering.order.service.domain.dto.create.CreateOrderResponse;
import org.ordering.order.service.domain.dto.track.TrackOrderQuery;
import org.ordering.order.service.domain.dto.track.TrackOrderResponse;

public interface OrderApplicationService {
    CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand);
    TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery);
}
