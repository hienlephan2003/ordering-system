package org.ordering.order.service.domain.outbox.scheduler.payment;

import org.ordering.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.ordering.order.service.domain.repository.PaymentOutboxRepository;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.ordering.saga.order.SagaConstants.ORDER_SAGA_NAME;

public class PaymentOutboxHelper {
    private final PaymentOutboxRepository paymentOutboxRepository;

    public PaymentOutboxHelper(PaymentOutboxRepository paymentOutboxRepository) {
        this.paymentOutboxRepository = paymentOutboxRepository;
    }

    public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus,
                                                                                                        SagaStatus... sagaStatus){
       return paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatus);
    }
    public void save(OrderPaymentOutboxMessage orderPaymentOutboxMessage){
        paymentOutboxRepository.save(orderPaymentOutboxMessage);
    }
    public void deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatus){
        paymentOutboxRepository.deleteByOutboxStatusAndSagaStatus(outboxStatus, sagaStatus);
    }
    public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID sagaId, SagaStatus... sagaStatus){
       return paymentOutboxRepository.findBySagaIdAndSagaStatus(sagaId, sagaStatus);
    }
}
