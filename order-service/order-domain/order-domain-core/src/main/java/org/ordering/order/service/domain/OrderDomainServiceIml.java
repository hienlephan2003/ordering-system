package org.ordering.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.entity.Product;
import org.ordering.order.service.domain.entity.Restaurant;
import org.ordering.order.service.domain.event.OrderCancelledEvent;
import org.ordering.order.service.domain.event.OrderCreatedEvent;
import org.ordering.order.service.domain.event.OrderPaidEvent;
import org.ordering.order.service.domain.exception.OrderDomainException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.ordering.domain.DomainConstants.UTC;

@Slf4j
public class OrderDomainServiceIml implements OrderDomainService{

    @Override
    public OrderCreatedEvent validateAndInitializeOrder(Order order, Restaurant restaurant) {
        //check if restaurant is active
        validateRestaurant(restaurant);
        //set product information (because order item have products and it value now only have id)
        setOrderProductInformation(order, restaurant);
        //validate order (is total money = sub item or not, is status is new)
        order.validateOrder();
        //initialize order ( create id for order, set order id for order item id)
        order.initializeOrder();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        return null;
    }

    @Override
    public void approveOrder(Order order) {

    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {
        return null;
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {

    }
    private void validateRestaurant(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue() +
                    " is currently not active!");
        }
    }
    private void setOrderProductInformation(Order order, Restaurant restaurant) {
        order.getItems().forEach(orderItem -> restaurant.getProducts().forEach(restaurantProduct -> {
            Product currentProduct = orderItem.getProduct();
            if (currentProduct.equals(restaurantProduct)) {
                currentProduct.updateWithConfirmedNameAndPrice(restaurantProduct.getName(),
                        restaurantProduct.getPrice());
            }
        }));
    }

}
