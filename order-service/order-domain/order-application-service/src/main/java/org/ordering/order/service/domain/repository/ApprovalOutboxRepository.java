package org.ordering.order.service.domain.repository;

import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;

import java.util.List;
import java.util.Optional;

public interface ApprovalOutboxRepository {
    Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(
            String type,
            OutboxStatus outboxStatus,
            SagaStatus... sagaStatus
    );
    Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(
            String type,
            SagaStatus sagaStatus,
            String sagaId
    );
    OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage message);
    void deleteByTypeAndOutboxStatusAndSagaStatus(String type,OutboxStatus outboxStatus, SagaStatus... sagaStatuses);
}
