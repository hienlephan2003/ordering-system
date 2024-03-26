package org.ordering.order.service.domain.mapper;

import org.ordering.domain.valueobject.*;
import org.ordering.order.service.domain.dto.create.CreateOrderCommand;
import org.ordering.order.service.domain.dto.create.CreateOrderResponse;
import org.ordering.order.service.domain.dto.create.OrderAddress;
import org.ordering.order.service.domain.dto.message.CustomerModel;
import org.ordering.order.service.domain.dto.track.TrackOrderResponse;
import org.ordering.order.service.domain.entity.*;
import org.ordering.order.service.domain.event.OrderCancelledEvent;
import org.ordering.order.service.domain.event.OrderPaidEvent;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalEventProduct;
import org.ordering.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import org.ordering.order.service.domain.valueobject.StreetAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Component
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
    public Customer customerModelToCustomer(CustomerModel customerModel) {
        return new Customer(new CustomerId((customerModel.getId())),
                customerModel.getUsername(),
                customerModel.getFirstName(),
                customerModel.getLastName());
    }
    public OrderApprovalEventPayload orderPaidEventToOrderApprovalEventPayload(OrderPaidEvent orderPaidEvent){
        return OrderApprovalEventPayload.builder()
                .orderId(orderPaidEvent.getOrder().toString())
                .restaurantOrderStatus(RestaurantOrderStatus.PAID.name())
                .restaurantId(orderPaidEvent.getOrder().getRestaurantId().toString())
                .price(orderPaidEvent.getOrder().getPrice().getAmount())
                .products(orderPaidEvent.getOrder().getItems().stream().map(orderItem ->
                        OrderApprovalEventProduct.builder()
                                .Id(orderItem.getProduct().getId().getValue().toString())
                                .quantity(orderItem.getQuantity())
                                .build()).collect(Collectors.toList()))
                .createdAt(orderPaidEvent.getCreatedAt())
                .build();
    }
    public OrderPaymentEventPayload orderCancelledEventToOrderPaymentEventPayload(OrderCancelledEvent orderCancelledEvent){
        return OrderPaymentEventPayload.builder()
                .customerId(orderCancelledEvent.getOrder().getCustomerId().getValue().toString())
                .orderId(orderCancelledEvent.getOrder().getId().getValue().toString())
                .price(orderCancelledEvent.getOrder().getPrice().getAmount())
                .createdAt(orderCancelledEvent.getCreatedAt())
                .paymentOrderStatus(PaymentOrderStatus.CANCELLED.name())
                .build();

    }
}
