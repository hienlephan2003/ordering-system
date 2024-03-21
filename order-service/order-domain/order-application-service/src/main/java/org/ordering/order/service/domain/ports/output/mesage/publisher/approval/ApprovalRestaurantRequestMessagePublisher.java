package org.ordering.order.service.domain.ports.output.mesage.publisher.approval;

import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.ordering.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface ApprovalRestaurantRequestMessagePublisher  {
    void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage,
                 BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback
                 );
}
