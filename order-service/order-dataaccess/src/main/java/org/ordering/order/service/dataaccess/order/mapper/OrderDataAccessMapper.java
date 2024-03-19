package org.ordering.order.service.dataaccess.order.mapper;

import org.ordering.domain.valueobject.*;
import org.ordering.order.service.dataaccess.order.entity.OrderAddressEntity;
import org.ordering.order.service.dataaccess.order.entity.OrderEntity;
import org.ordering.order.service.dataaccess.order.entity.OrderItemEntity;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.entity.OrderItem;
import org.ordering.order.service.domain.entity.Product;
import org.ordering.order.service.domain.valueobject.OrderItemId;
import org.ordering.order.service.domain.valueobject.StreetAddress;
import org.ordering.order.service.domain.valueobject.TrackingId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ordering.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

public class OrderDataAccessMapper {
    public OrderEntity orderToOrderEntity(Order order){
        OrderEntity orderEntity =  OrderEntity.builder()
                .id(order.getId().getValue())
                .address(deliveryAddressToOrderAddress(order.getDeliveryAddress()))
                .orderStatus(order.getOrderStatus())
                .items(order.getItems().stream().map(item ->
                                OrderItemEntity.builder()
                                        .quantity(item.getQuantity())
                                        .subTotal(item.getSubTotal().getAmount())
                                        .productId(item.getProduct().getId().getValue())
                                        .price(item.getPrice().getAmount())
                                        .build()
                        ).collect(Collectors.toList()))
                .price(order.getPrice().getAmount())
                .failureMessages(order.getFailureMessages() != null ?
                        String.join(FAILURE_MESSAGE_DELIMITER, order.getFailureMessages()) : "")
                .restaurantId(order.getRestaurantId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .customerId(order.getCustomerId().getValue())
                .build();
            orderEntity.getAddress().setOrder(orderEntity);
            orderEntity.getItems().forEach(item -> item.setOrder(orderEntity));
            return orderEntity;
    }
    public Order orderEntityToOrder(OrderEntity orderEntity){
        return Order.builder()
                .orderId(new OrderId(orderEntity.getId()))
                .customerId(new CustomerId(orderEntity.getCustomerId()))
                .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
                .deliveryAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
                .price(new Money(orderEntity.getPrice()))
                .items(orderItemEntitiesToOrderItems(orderEntity.getItems()))
                .trackingId(new TrackingId(orderEntity.getTrackingId()))
                .orderStatus(orderEntity.getOrderStatus())
                .failureMessages(orderEntity.getFailureMessages().isEmpty() ? new ArrayList<>() :
                        new ArrayList<>(Arrays.asList(orderEntity.getFailureMessages()
                                .split(FAILURE_MESSAGE_DELIMITER))))
                .build();
    }
    public OrderAddressEntity deliveryAddressToOrderAddress(StreetAddress streetAddress){
        return OrderAddressEntity.builder()
                .city(streetAddress.getCity())
                .postalCode(streetAddress.getPostalCode())
                .street(streetAddress.getStreet())
                .id(streetAddress.getId())
                .build();
    }
    private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity address) {
        return new StreetAddress(address.getId(),
                address.getStreet(),
                address.getPostalCode(),
                address.getCity());
    }
    private List<OrderItem> orderItemEntitiesToOrderItems(List<OrderItemEntity> items) {
        return items.stream()
                .map(orderItemEntity -> OrderItem.builder()
                        .orderItemId(new OrderItemId(orderItemEntity.getId()))
                        .product(new Product(new ProductId(orderItemEntity.getProductId())))
                        .price(new Money(orderItemEntity.getPrice()))
                        .quantity(orderItemEntity.getQuantity())
                        .subTotal(new Money(orderItemEntity.getSubTotal()))
                        .build())
                .collect(Collectors.toList());
    }

}
