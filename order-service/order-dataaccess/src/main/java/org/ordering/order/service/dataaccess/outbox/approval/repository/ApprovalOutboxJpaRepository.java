package org.ordering.order.service.dataaccess.outbox.approval.repository;

import org.ordering.domain.valueobject.OrderStatus;
import org.ordering.order.service.dataaccess.outbox.approval.entity.ApprovalOutboxMessageEntity;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ApprovalOutboxJpaRepository extends JpaRepository<ApprovalOutboxMessageEntity, UUID> {
    void deleteByTypeAndOutboxStatusAndSagaStatusIn(String type, OutboxStatus outboxStatus, Collection<SagaStatus> sagaStatus);
    Optional<ApprovalOutboxMessageEntity> findByTypeAndSagaIdAndSagaStatusIn(String type, UUID sagaId, Collection<SagaStatus> sagaStatus);
    Optional<List<ApprovalOutboxMessageEntity>>
        findByTypeAndOutboxStatusAndSagaStatusIn(String type, OutboxStatus outboxStatus, Collection<SagaStatus> sagaStatus);
}
