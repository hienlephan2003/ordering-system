package org.ordering.order.service.messaging.listerner.kafka;


import lombok.extern.slf4j.Slf4j;
import org.ordering.kafka.consumer.KafkaConsumer;
import org.ordering.kafka.order.avro.model.PaymentResponseAvroModel;
import org.ordering.kafka.order.avro.model.PaymentStatus;
import org.ordering.order.service.domain.exception.OrderNotFoundException;
import org.ordering.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import org.ordering.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

@Slf4j
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseAvroModel> {
    private final PaymentResponseMessageListener paymentResponseMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    public PaymentResponseKafkaListener(PaymentResponseMessageListener paymentResponseMessageListener, OrderMessagingDataMapper orderMessagingDataMapper) {
        this.paymentResponseMessageListener = paymentResponseMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }

    @Override
    public void receive(List<PaymentResponseAvroModel> messages, List<String> keys, List<Integer> partitions, List<Long> offsets) {
        messages.forEach(paymentResponseAvroModel ->{
            try {
                if (paymentResponseAvroModel.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    paymentResponseMessageListener.paymentCompleted(
                            orderMessagingDataMapper.paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
                } else {
                    log.info("Processing unsuccessful payment for order id: {}", paymentResponseAvroModel.getOrderId());
                    paymentResponseMessageListener.paymentCancelled(orderMessagingDataMapper
                            .paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
                }
            }catch (OptimisticLockingFailureException e) {
                log.error("Caught optimistic locking exception in PaymentResponseKafkaListener for order id: {}",
                        paymentResponseAvroModel.getOrderId());
            } catch (OrderNotFoundException e) {
                log.error("No order found for order id: {}", paymentResponseAvroModel.getOrderId());
            }
        });
    }
}
