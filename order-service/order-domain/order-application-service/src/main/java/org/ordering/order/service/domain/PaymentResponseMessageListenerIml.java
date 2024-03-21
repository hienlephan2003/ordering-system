package org.ordering.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.ordering.domain.valueobject.OrderId;
import org.ordering.domain.valueobject.PaymentStatus;
import org.ordering.order.service.domain.dto.message.PaymentResponse;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.event.OrderPaidEvent;
import org.ordering.order.service.domain.mapper.OrderDataMapper;
import org.ordering.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.ordering.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import org.ordering.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import org.ordering.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import org.ordering.order.service.domain.repository.OrderRepository;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.ordering.domain.DomainConstants.UTC;

@Slf4j
public class PaymentResponseMessageListenerIml implements PaymentResponseMessageListener {
    //outbox helper for update outbox message
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    //order domain service for update service logic
    private final OrderDomainService orderDomainService;
    // order saga helper for get saga status from order status
    private final OrderSagaHelper orderSagaHelper;
    //order data mapper for mapping from event to event payload
    //order repository to connect to db
    private final OrderRepository orderRepository;
    private final OrderDataMapper orderDataMapper;
    public PaymentResponseMessageListenerIml(PaymentOutboxHelper paymentOutboxHelper, ApprovalOutboxHelper approvalOutboxHelper, OrderDomainService orderDomainService, OrderSagaHelper orderSagaHelper, OrderRepository orderRepository, OrderDataMapper orderDataMapper) {
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.orderRepository = orderRepository;
        this.orderDataMapper = orderDataMapper;
    }

    @Override
    public void paymentCompleted(PaymentResponse paymentResponse) {
        //get order payment outbox message
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(paymentResponse.getSagaId()), SagaStatus.STARTED);
        if(orderPaymentOutboxMessageResponse.isEmpty()){
            log.info("An outbox message with saga id: {} is already processed!", paymentResponse.getSagaId());
            return;
        }
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();
        //create order paid event
        Optional<Order> order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order.get());
        // get new saga status from order status
        SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(orderPaidEvent.getOrder().getOrderStatus());
        //update outbox message and save
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderPaymentOutboxMessage.setOrderStatus(orderPaidEvent.getOrder().getOrderStatus());
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        paymentOutboxHelper.save(orderPaymentOutboxMessage);
        //create approval outbox message and save
        approvalOutboxHelper.saveApprovalOutboxMessage(orderDataMapper.orderPaidEventToOrderApprovalEventPayload(orderPaidEvent),
                orderPaidEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(paymentResponse.getSagaId())
                );
        //log result
        log.info("Order with id: {} is paid", orderPaidEvent.getOrder().getId().getValue());
    }

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {
        // get payment request outbox message response
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessage = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(paymentResponse.getSagaId()),
                getCurrentSagaStatus(paymentResponse.getPaymentStatus())
        );
        //if empty -> already roll backed.
        if(orderPaymentOutboxMessage.isEmpty()){
            log.error("");
        }
        //get payment request outbox message
        //do service logic: rollback payment for order
        //get saga status from order status
        //update payment request outbox message
        //update approval outbox message
        //log result
    }
    private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case COMPLETED -> new SagaStatus[] { SagaStatus.STARTED };
            case CANCELLED -> new SagaStatus[] { SagaStatus.PROCESSING };
            case FAILED -> new SagaStatus[] { SagaStatus.STARTED, SagaStatus.PROCESSING };
        };
    }

}
