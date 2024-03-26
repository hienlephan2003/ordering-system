package org.ordering.order.service.dataaccess.outbox.approval.mapper;

import org.ordering.order.service.dataaccess.outbox.approval.entity.ApprovalOutboxMessageEntity;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderApprovalDataAccessMapper {
    public OrderApprovalOutboxMessage
       approvalOutboxMessageEntityToorderApprovalOutboxMessage(
               ApprovalOutboxMessageEntity entity
    ){
        return OrderApprovalOutboxMessage.builder()
                .id(entity.getId())
                .sagaId(entity.getSagaId())
                .outboxStatus(entity.getOutboxStatus())
                .type(entity.getType())
                .payload(entity.getPayload())
                .sagaStatus(entity.getSagaStatus())
                .orderStatus(entity.getOrderStatus())
                .processedAt(entity.getProcessedAt())
                .createdAt(entity.getCreatedAt())
                .version(entity.getVersion())
                .build();
    }
    public ApprovalOutboxMessageEntity orderApprovalOutboxMessageToApprovalOutboxMessageEntity(
            OrderApprovalOutboxMessage message
    ){
        return ApprovalOutboxMessageEntity.builder()
                .sagaId(message.getSagaId())
                .id(message.getId())
                .processedAt(message.getProcessedAt())
                .createdAt(message.getCreatedAt())
                .orderStatus(message.getOrderStatus())
                .outboxStatus(message.getOutboxStatus())
                .sagaStatus(message.getSagaStatus())
                .payload(message.getPayload())
                .type(message.getType())
                .version(message.getVersion())
                .build();
    }
}
