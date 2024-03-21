package org.ordering.order.service.domain.outbox.scheduler.approval;

import lombok.extern.slf4j.Slf4j;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.ordering.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.ordering.order.service.domain.ports.output.mesage.publisher.approval.ApprovalRestaurantRequestMessagePublisher;
import org.ordering.order.service.domain.ports.output.mesage.publisher.payment.PaymentRequestMessagePublisher;
import org.ordering.outbox.OutboxScheduler;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class PaymentOutboxScheduler implements OutboxScheduler {
    private final ApprovalRestaurantRequestMessagePublisher approvalRestaurantRequestMessagePublisher;
    private final ApprovalOutboxHelper approvalOutboxHelper;

    public PaymentOutboxScheduler(ApprovalRestaurantRequestMessagePublisher approvalRestaurantRequestMessagePublisher, ApprovalOutboxHelper approvalOutboxHelper) {
        this.approvalRestaurantRequestMessagePublisher = approvalRestaurantRequestMessagePublisher;
        this.approvalOutboxHelper = approvalOutboxHelper;
    }

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage(){
       Optional<List<OrderApprovalOutboxMessage>> outboxMessagesResponse = approvalOutboxHelper.getApprovalOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.STARTED, SagaStatus.STARTED, SagaStatus.COMPENSATING);
        if (outboxMessagesResponse.isPresent() && outboxMessagesResponse.get().size() > 0) {
            List<OrderApprovalOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} OrderApprovalOutboxMessage with ids: {}, sending to message bus!",
                    outboxMessages.size(),
                    outboxMessages.stream().map(outboxMessage ->
                            outboxMessage.getId().toString()).collect(Collectors.joining(",")));
            outboxMessages.forEach(outboxMessage ->
                    approvalRestaurantRequestMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));
            log.info("{} OrderApprovalOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }
    private void updateOutboxStatus(OrderApprovalOutboxMessage orderApprovalOutboxMessage, OutboxStatus outboxStatus){
        orderApprovalOutboxMessage.setOutboxStatus(outboxStatus);
        approvalOutboxHelper.save(orderApprovalOutboxMessage);
        log.info("OrderApprovalOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }
}
