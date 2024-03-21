package org.ordering.order.service.domain.outbox.scheduler.approval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.ordering.domain.valueobject.OrderStatus;
import org.ordering.order.service.domain.exception.OrderDomainException;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.ordering.order.service.domain.repository.ApprovalOutboxRepository;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.ordering.saga.order.SagaConstants.ORDER_SAGA_NAME;


    @Slf4j
    public  class ApprovalOutboxHelper {
        private final ApprovalOutboxRepository approvalOutboxRepository;
        private final ObjectMapper objectMapper;

        public ApprovalOutboxHelper(ApprovalOutboxRepository approvalOutboxRepository, ObjectMapper objectMapper) {
            this.approvalOutboxRepository = approvalOutboxRepository;
            this.objectMapper = objectMapper;
        }
        @Transactional(readOnly = true)
        public Optional<List<OrderApprovalOutboxMessage>> getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus outboxStatus,
                SagaStatus... sagaStatuses
        ){
            return approvalOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME,outboxStatus, sagaStatuses);
        }
        @Transactional(readOnly = true)
        public Optional<OrderApprovalOutboxMessage> getApprovalOutboxMessageBySagaStatusAndSagaId(
                SagaStatus sagaStatus,
                String sagaId
        ){
            return approvalOutboxRepository.findByTypeAndSagaIdAndOutboxStatus(ORDER_SAGA_NAME, sagaStatus, sagaId);
        }
        @Transactional
        public void save(OrderApprovalOutboxMessage orderApprovalOutboxMessage){
            OrderApprovalOutboxMessage orderApprovalOutboxMessageResponse = approvalOutboxRepository.save(orderApprovalOutboxMessage);
            if(orderApprovalOutboxMessageResponse == null){
                log.error("Could not save OrderApprovalOutboxMessage with outbox id {}", orderApprovalOutboxMessage.getId());
                throw new OrderDomainException("Could not save OrderApprovalOutboxMessage with outbox id"+ orderApprovalOutboxMessage.getId());
            }
            log.info("OrderApprovalOutboxMessage saved with outbox id: {}", orderApprovalOutboxMessage.getId());
        }
        @Transactional
        public void saveApprovalOutboxMessage(
                OrderApprovalEventPayload orderApprovalEventPayload,
                                              OrderStatus orderStatus,
                                              SagaStatus sagaStatus,
                                              OutboxStatus outboxStatus,
                                                    UUID sagaId
                                              ){
            save(OrderApprovalOutboxMessage.builder()
                    .id(UUID.randomUUID())
                    .sagaId(sagaId)
                    .orderStatus(orderStatus)
                    .sagaStatus(sagaStatus)
                    .outboxStatus(outboxStatus)
                    .type(ORDER_SAGA_NAME)
                    .payload(createPayload(orderApprovalEventPayload))
                    .build());

        }
        private String createPayload(OrderApprovalEventPayload orderApprovalEventPayload){
            try {
                return objectMapper.writeValueAsString(orderApprovalEventPayload);
            } catch (JsonProcessingException e) {
                log.error("Could not create OrderApprovalEventPayload for order id: {}",
                        orderApprovalEventPayload.getOrderId(), e);
                throw new OrderDomainException("Could not create OrderApprovalEventPayload for order id: " +
                        orderApprovalEventPayload.getOrderId(), e);
            }
        }
        @Transactional
        public void deleteApprovalOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus,
                                                                           SagaStatus... sagaStatus) {
            approvalOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatus);
        }

    }

