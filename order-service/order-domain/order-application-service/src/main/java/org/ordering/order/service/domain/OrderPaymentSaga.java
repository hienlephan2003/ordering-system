package org.ordering.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.ordering.domain.valueobject.OrderId;
import org.ordering.domain.valueobject.OrderStatus;
import org.ordering.domain.valueobject.PaymentStatus;
import org.ordering.order.service.domain.dto.message.PaymentResponse;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.event.OrderPaidEvent;
import org.ordering.order.service.domain.exception.OrderDomainException;
import org.ordering.order.service.domain.exception.OrderNotFoundException;
import org.ordering.order.service.domain.mapper.OrderDataMapper;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.ordering.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.ordering.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import org.ordering.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import org.ordering.order.service.domain.repository.OrderRepository;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;
import org.ordering.saga.SagaStep;

import java.rmi.server.UID;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.ordering.domain.DomainConstants.UTC;

@Slf4j
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderSagaHelper orderSagaHelper;
    private final OrderDomainService orderDomainService;
    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    public OrderPaymentSaga(PaymentOutboxHelper paymentOutboxHelper, ApprovalOutboxHelper approvalOutboxHelper, OrderSagaHelper orderSagaHelper, OrderDomainService orderDomainService, OrderDataMapper orderDataMapper, OrderRepository orderRepository) {
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderSagaHelper = orderSagaHelper;
        this.orderDomainService = orderDomainService;
        this.orderDataMapper = orderDataMapper;
        this.orderRepository = orderRepository;
    }

    @Override
    public void process(PaymentResponse data) {
        //get payment request outbox message
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(data.getSagaId()),
                        getSagaStatusFromPaymentStatus(data.getPaymentStatus())
                );
        //if empty -> throw exception
        if(orderPaymentOutboxMessageResponse.isEmpty()){
            log.info("An outbox message with saga id: {} is already processed!", data.getSagaId());
            return;
        }
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();
        //update domain business logic and get order paid event
        Order order = findOrderById(data.getOrderId());
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);
        orderRepository.save(order);
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(orderPaidEvent.getOrder().getOrderStatus());
        //update payment request outbox message
        orderPaymentOutboxMessage.setOrderStatus(orderPaidEvent.getOrder().getOrderStatus());
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        paymentOutboxHelper.save(orderPaymentOutboxMessage);
        //create order approval outbox message
        approvalOutboxHelper.saveApprovalOutboxMessage(orderDataMapper.orderPaidEventToOrderApprovalEventPayload(orderPaidEvent),
                orderPaidEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(data.getSagaId())
                );
        //log information
        log.info("Order with id: {} is paid", orderPaidEvent.getOrder().getId().getValue());
    }
    //rollback when payment fail or canceled order. if canceled order, need to update
    //order approval outbox message
    @Override
    public void rollback(PaymentResponse data) {
        //get payment request outbox message
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                    UUID.fromString(data.getSagaId()),
                        getSagaStatusFromPaymentStatus(data.getPaymentStatus())
        );
        //if empty -> throw exception
        if(orderPaymentOutboxMessageResponse.isEmpty()){
            return;
        }
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();
        //get order from db and do business logic
        Order order = findOrderById(data.getOrderId());
        orderDomainService.cancelOrder(order, data.getFailureMessages());
        orderRepository.save(order);
        //get new saga status to update outbox message
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
        //update payment request outbox message
        orderPaymentOutboxMessage.setOrderStatus(order.getOrderStatus());
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        paymentOutboxHelper.save(orderPaymentOutboxMessage);
        // if canceled order, update order approval outbox message
        if(data.getPaymentStatus() == PaymentStatus.CANCELLED){
            approvalOutboxHelper.save(getUpdateApprovalOutboxMessage(data.getSagaId(),order.getOrderStatus(), sagaStatus));
        }
    }
    private OrderApprovalOutboxMessage getUpdateApprovalOutboxMessage(
            String sagaId,
            OrderStatus orderStatus,
            SagaStatus sagaStatus
    ){
        //find order approval outbox message
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse = approvalOutboxHelper.getApprovalOutboxMessageBySagaStatusAndSagaId(
                SagaStatus.COMPENSATING,
                sagaId
        );
        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            throw new OrderDomainException("Approval outbox message could not be found in " +
                    SagaStatus.COMPENSATING.name() + " status!");
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();
        //update status
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        return orderApprovalOutboxMessage;
    }
    private Order findOrderById(String orderId){
        Optional<Order> order = orderRepository.findById(new OrderId(UUID.fromString(orderId)));
        if(order.isEmpty()){
            log.error("Order with id: {} could not be found!", orderId);
            throw new OrderNotFoundException("Order with id " + orderId + " could not be found!");
        }
        return order.get();
    }
    private SagaStatus[] getSagaStatusFromPaymentStatus(PaymentStatus paymentStatus){
       return switch (paymentStatus){
            case COMPLETED ->
                 new SagaStatus[]{SagaStatus.STARTED};
            case CANCELLED ->
                 new SagaStatus[]{SagaStatus.PROCESSING};
            case FAILED ->
                 new SagaStatus[]{SagaStatus.STARTED, SagaStatus.PROCESSING};
        };
    }
}
