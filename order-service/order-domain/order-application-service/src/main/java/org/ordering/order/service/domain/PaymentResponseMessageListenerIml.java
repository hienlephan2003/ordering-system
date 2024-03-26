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
import static org.ordering.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

@Slf4j
public class PaymentResponseMessageListenerIml implements PaymentResponseMessageListener {
    private final OrderPaymentSaga orderPaymentSaga;

    public PaymentResponseMessageListenerIml(OrderPaymentSaga orderPaymentSaga) {
        this.orderPaymentSaga = orderPaymentSaga;
    }

    @Override
    public void paymentCompleted(PaymentResponse paymentResponse) {
        orderPaymentSaga.process(paymentResponse);
        log.info("Order payment saga process operation is completed for order id {}", paymentResponse.getOrderId());
    }

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {
        orderPaymentSaga.rollback(paymentResponse);
        log.info("Order is roll backed for order id: {} with failure messages: {}",
                paymentResponse.getOrderId(),
                String.join(FAILURE_MESSAGE_DELIMITER, paymentResponse.getFailureMessages()));
    }
}
