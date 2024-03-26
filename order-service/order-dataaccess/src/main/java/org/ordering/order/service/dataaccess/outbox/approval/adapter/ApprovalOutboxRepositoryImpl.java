package org.ordering.order.service.dataaccess.outbox.approval.adapter;

import org.ordering.order.service.dataaccess.outbox.approval.exception.OrderApprovalOutboxException;
import org.ordering.order.service.dataaccess.outbox.approval.mapper.OrderApprovalDataAccessMapper;
import org.ordering.order.service.dataaccess.outbox.approval.repository.ApprovalOutboxJpaRepository;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.ordering.order.service.domain.repository.ApprovalOutboxRepository;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ApprovalOutboxRepositoryImpl implements ApprovalOutboxRepository {
    private final OrderApprovalDataAccessMapper mapper;
    private final ApprovalOutboxJpaRepository jpaRepository;

    public ApprovalOutboxRepositoryImpl(OrderApprovalDataAccessMapper mapper, ApprovalOutboxJpaRepository jpaRepository) {
        this.mapper = mapper;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        return
               Optional.of( jpaRepository.findByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatus))
                        .orElseThrow(() -> new OrderApprovalOutboxException("Approval outbox object " +
                                "could be found for saga type " + type))
                        .stream()
                        .map(mapper::approvalOutboxMessageEntityToorderApprovalOutboxMessage).collect(Collectors.toList()));
    }

    @Override
    public Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(String type, UUID sagaId, SagaStatus... sagaStatus) {
        return
                jpaRepository.findByTypeAndSagaIdAndSagaStatusIn(type,sagaId, Arrays.asList(sagaStatus)).map(
                        mapper::approvalOutboxMessageEntityToorderApprovalOutboxMessage
                );
    }

    @Override
    public OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage message) {
        return mapper.approvalOutboxMessageEntityToorderApprovalOutboxMessage(
                jpaRepository.save(mapper.orderApprovalOutboxMessageToApprovalOutboxMessageEntity(message))
        );
    }

    @Override
    public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        jpaRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatuses));
    }
}
