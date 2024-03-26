package org.ordering.order.service.dataaccess.outbox.payment.repository;

import org.ordering.order.service.dataaccess.outbox.payment.model.PaymentOutboxEntity;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface PaymentOutboxJpaRepository extends JpaRepository<PaymentOutboxEntity, UUID> {
    Optional<List<PaymentOutboxEntity>> findPaymentOutboxEntityByTypeAndOutboxStatusAndSagaStatusIn(
            String type, OutboxStatus outboxStatus, Collection<SagaStatus> sagaStatus);
    Optional<List<PaymentOutboxEntity>> findPaymentOutboxEntityByTypeAndOutboxStatus(
            String type, OutboxStatus outboxStatus);

    Optional<PaymentOutboxEntity> findPaymentOutboxEntityByTypeAndSagaIdAndSagaStatusIn(
            String type, UUID sagaId, Collection<SagaStatus> sagaStatus);
    Optional<PaymentOutboxEntity> findPaymentOutboxEntityByTypeAndSagaIdAndSagaStatus(
            String type, UUID sagaId, SagaStatus sagaStatus);

    Optional<PaymentOutboxEntity> findPaymentOutboxEntityByTypeAndSagaId(
            String type, UUID sagaId);

    void deletePaymentOutboxEntityByTypeAndOutboxStatusAndSagaStatusIn(
            String type, OutboxStatus outboxStatus, Collection<SagaStatus> sagaStatus);

}
