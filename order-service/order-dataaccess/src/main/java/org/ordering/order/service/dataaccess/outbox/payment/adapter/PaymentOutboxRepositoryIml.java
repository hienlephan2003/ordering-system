package org.ordering.order.service.dataaccess.outbox.payment.adapter;

import org.ordering.order.service.dataaccess.outbox.payment.exception.PaymentOutboxNotFoundException;
import org.ordering.order.service.dataaccess.outbox.payment.mapper.PaymentOutboxDataAccessMapper;
import org.ordering.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import org.ordering.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.ordering.order.service.domain.repository.PaymentOutboxRepository;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Component
public class PaymentOutboxRepositoryIml implements PaymentOutboxRepository {
    private final PaymentOutboxJpaRepository paymentOutboxJpaRepository;
    private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

    public PaymentOutboxRepositoryIml(PaymentOutboxJpaRepository paymentOutboxJpaRepository, PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper) {
        this.paymentOutboxJpaRepository = paymentOutboxJpaRepository;
        this.paymentOutboxDataAccessMapper = paymentOutboxDataAccessMapper;
    }

    @Override
    public Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatus) {
        return Optional.of(paymentOutboxJpaRepository.findPaymentOutboxEntityByTypeAndOutboxStatusAndSagaStatusIn(type,
                        outboxStatus,
                        Arrays.asList(sagaStatus))
                .orElseThrow(() -> new PaymentOutboxNotFoundException("Payment outbox object " +
                        "could not be found for saga type " + type))
                .stream()
                .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage)
                .collect(Collectors.toList()));
    }

    @Override
    public OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage message) {
        return paymentOutboxDataAccessMapper.paymentOutboxEntityToOrderPaymentOutboxMessage( paymentOutboxJpaRepository.save(
            paymentOutboxDataAccessMapper.orderPaymentOutboxMessageToOutboxEntity(message)
        ));
    }

    @Override
    public void deleteByTypeAndOutboxStatusAndSagaStatus(String type,OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        paymentOutboxJpaRepository.deletePaymentOutboxEntityByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.stream(sagaStatuses).toList());
    }

    @Override
    public Optional<OrderPaymentOutboxMessage> findBySagaIdAndSagaStatus(String type,UUID sagaId, SagaStatus... sagaStatus) {
        return
                paymentOutboxJpaRepository
                        .findPaymentOutboxEntityByTypeAndSagaIdAndSagaStatusIn(type ,sagaId, Arrays.asList(sagaStatus))
                        .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage);
    }
}
