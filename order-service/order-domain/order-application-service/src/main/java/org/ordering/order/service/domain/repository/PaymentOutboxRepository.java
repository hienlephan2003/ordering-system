package org.ordering.order.service.domain.repository;

import org.ordering.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentOutboxRepository {
    Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(
            String type,
            OutboxStatus outboxStatus,
            SagaStatus... sagaStatus
    );
    OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage message);
    void deleteByTypeAndOutboxStatusAndSagaStatus(String type,OutboxStatus outboxStatus, SagaStatus... sagaStatuses);
    Optional<OrderPaymentOutboxMessage> findBySagaIdAndSagaStatus(String type, UUID sagaId, SagaStatus... sagaStatus);
}
