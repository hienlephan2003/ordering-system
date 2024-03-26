package org.ordering.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.ordering.domain.valueobject.OrderApprovalStatus;
import org.ordering.domain.valueobject.OrderId;
import org.ordering.domain.valueobject.OrderStatus;
import org.ordering.order.service.domain.dto.message.RestaurantApprovalResponse;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.event.OrderCancelledEvent;
import org.ordering.order.service.domain.exception.OrderDomainException;
import org.ordering.order.service.domain.mapper.OrderDataMapper;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.ordering.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import org.ordering.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import org.ordering.order.service.domain.repository.OrderRepository;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;
import org.ordering.saga.SagaStep;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.ordering.domain.DomainConstants.UTC;
@Slf4j
@Component
public class RestaurantApprovalSaga implements SagaStep<RestaurantApprovalResponse> {
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final OrderSagaHelper orderSagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final OrderDataMapper orderDataMapper;

    public RestaurantApprovalSaga(ApprovalOutboxHelper approvalOutboxHelper, OrderDomainService orderDomainService, OrderRepository orderRepository, OrderSagaHelper orderSagaHelper, PaymentOutboxHelper paymentOutboxHelper, OrderDataMapper orderDataMapper) {
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDomainService = orderDomainService;
        this.orderRepository = orderRepository;
        this.orderSagaHelper = orderSagaHelper;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.orderDataMapper = orderDataMapper;
    }

    @Override
    public void process(RestaurantApprovalResponse data) {
        //get order approval outbox message
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse = approvalOutboxHelper.getApprovalOutboxMessageBySagaStatusAndSagaId(
                data.getSagaId(),
                orderApprovalStattusToSagaStatus(data.getOrderApprovalStatus())
        );
        //if empty -> throw exception
        if(orderApprovalOutboxMessageResponse.isEmpty()){
            return;
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();
        //update business logic
        Order order = findOrderById(data.getOrderId());
        orderDomainService.approveOrder(order);
        orderSagaHelper.saveOrder(order);
        //get new saga status
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
        //update order approval outbox message
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderApprovalOutboxMessage.setOrderStatus(order.getOrderStatus());
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        approvalOutboxHelper.save(orderApprovalOutboxMessage);
        log.info("Order with id: {} is approved", order.getId().getValue());
    }
    @Override
    @Transactional
    public void rollback(RestaurantApprovalResponse data) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaStatusAndSagaId(
                        data.getSagaId(),
                        SagaStatus.PROCESSING);

        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed!",
                    data.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();

        OrderCancelledEvent domainEvent = rollbackOrder(data);

        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(domainEvent.getOrder().getOrderStatus());

        approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage,
                domainEvent.getOrder().getOrderStatus(), sagaStatus));

        paymentOutboxHelper.savePaymentOutboxMessage(orderDataMapper
                        .orderCancelledEventToOrderPaymentEventPayload(domainEvent),
                domainEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                (data.getSagaId()));

        log.info("Order with id: {} is cancelling", domainEvent.getOrder().getId().getValue());

    }
    private Order findOrderById(UUID id){
        Optional<Order> order = orderRepository.findById(new OrderId((id)));
        if(order.isEmpty()){
            throw new OrderDomainException("Not found order with id" + id);
        }
        return order.get();
    }
    private SagaStatus[] orderApprovalStattusToSagaStatus(OrderApprovalStatus status){
        return switch (status){
            case APPROVED, REJECTED -> new SagaStatus[]{SagaStatus.PROCESSING};
        };
    }
    private OrderCancelledEvent rollbackOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
        log.info("Cancelling order with id: {}", restaurantApprovalResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
        OrderCancelledEvent domainEvent = orderDomainService.cancelOrderPayment(order,
                restaurantApprovalResponse.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        return domainEvent;
    }
    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(OrderApprovalOutboxMessage
                                                                               orderApprovalOutboxMessage,
                                                                       OrderStatus
                                                                               orderStatus,
                                                                       SagaStatus
                                                                               sagaStatus) {
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        return orderApprovalOutboxMessage;
    }

}
