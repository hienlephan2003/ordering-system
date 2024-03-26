package org.ordering.order.service.domain;

import org.ordering.order.service.domain.dto.message.RestaurantApprovalResponse;
import org.ordering.order.service.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import org.springframework.stereotype.Component;

@Component
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalResponseMessageListener {
    private final RestaurantApprovalSaga restaurantApprovalSaga;

    public RestaurantApprovalResponseMessageListenerImpl(RestaurantApprovalSaga restaurantApprovalSaga) {
        this.restaurantApprovalSaga = restaurantApprovalSaga;
    }

    @Override
    public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {

    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {

    }
}
