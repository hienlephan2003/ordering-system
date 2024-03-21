package org.ordering.order.service.domain.outbox.model.approval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.ordering.domain.valueobject.OrderStatus;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;

import java.time.ZonedDateTime;
import java.util.UUID;
@Getter
@Builder
@AllArgsConstructor
public class OrderApprovalOutboxMessage {
    private UUID id;
    private UUID sagaId;
    private ZonedDateTime createdAt;
    private ZonedDateTime processedAt;
    private String type;
    private String payload;
    private SagaStatus sagaStatus;
    private OutboxStatus outboxStatus;
    private int version;
    private OrderStatus orderStatus;

    public void setProcessedAt(ZonedDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public void setSagaStatus(SagaStatus sagaStatus) {
        this.sagaStatus = sagaStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setOutboxStatus(OutboxStatus outboxStatus) {
        this.outboxStatus = outboxStatus;
    }
}
