package org.ordering.order.service.domain.mapper;

import org.ordering.domain.valueobject.CustomerId;
import org.ordering.domain.valueobject.Money;
import org.ordering.domain.valueobject.ProductId;
import org.ordering.domain.valueobject.RestaurantId;
import org.ordering.order.service.domain.dto.create.CreateOrderCommand;
import org.ordering.order.service.domain.dto.create.CreateOrderResponse;
import org.ordering.order.service.domain.dto.create.OrderAddress;
import org.ordering.order.service.domain.dto.track.TrackOrderResponse;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.entity.OrderItem;
import org.ordering.order.service.domain.entity.Product;
import org.ordering.order.service.domain.entity.Restaurant;
import org.ordering.order.service.domain.valueobject.StreetAddress;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderDataMapper {
    public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand){
        return Restaurant.builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(createOrderCommand.getItems().stream().map(orderItem ->
                        new Product(new ProductId(orderItem.getProductId()))).collect(Collectors.toList()))
                .build();
    }
    public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand){
        return Order.builder()
                .customerId(new CustomerId(createOrderCommand.getCustomerId()))
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .deliveryAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
                .price(new Money(createOrderCommand.getPrice()))
                .items(orderItemsToOrderItemEntities(createOrderCommand.getItems())).build();
    }
    public CreateOrderResponse orderToCreateOrderResponse(Order order, String messages){
        return CreateOrderResponse.builder()
                .orderStatus(order.getOrderStatus())
                .message(messages)
                .orderTrackingId(order.getTrackingId().getValue())
                .build();
    }
    public TrackOrderResponse orderToTrackOrderResponse(Order order){
        return TrackOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus())
                .failureMessages(order.getFailureMessages())
                .build();
    }
    private List<OrderItem> orderItemsToOrderItemEntities(
            List<org.ordering.order.service.domain.dto.create.OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem ->
                        OrderItem.builder()
                                .product(new Product(new ProductId(orderItem.getProductId())))
                                .price(new Money(orderItem.getPrice()))
                                .quantity(orderItem.getQuantity())
                                .subTotal(new Money(orderItem.getSubTotal()))
                                .build()).collect(Collectors.toList());
    }

    private StreetAddress orderAddressToStreetAddress(OrderAddress orderAddress) {
        return new StreetAddress(
                UUID.randomUUID(),
                orderAddress.getStreet(),
                orderAddress.getPostalCode(),
                orderAddress.getCity()
        );
    }

}
