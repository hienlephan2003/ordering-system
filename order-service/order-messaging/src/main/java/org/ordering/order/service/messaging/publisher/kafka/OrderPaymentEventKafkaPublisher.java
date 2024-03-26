package org.ordering.order.service.messaging.publisher.kafka;

import lombok.extern.slf4j.Slf4j;
import org.ordering.kafka.order.avro.model.PaymentRequestAvroModel;
import org.ordering.kafka.producer.KafkaMessageHelper;
import org.ordering.kafka.producer.service.KafkaProducer;
import org.ordering.order.service.domain.config.OrderServiceConfigData;
import org.ordering.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import org.ordering.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.ordering.order.service.domain.ports.output.mesage.publisher.payment.PaymentRequestMessagePublisher;
import org.ordering.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.ordering.outbox.OutboxStatus;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;
@Slf4j
@Component
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;
    public OrderPaymentEventKafkaPublisher(KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer, OrderMessagingDataMapper orderMessagingDataMapper, OrderServiceConfigData orderServiceConfigData, KafkaMessageHelper kafkaMessageHelper) {
        this.kafkaProducer = kafkaProducer;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage, BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback) {
        String topicName = orderServiceConfigData.getPaymentRequestTopicName();
        String sagaId = orderPaymentOutboxMessage.getSagaId().toString();
        OrderPaymentEventPayload payload = kafkaMessageHelper.getOrderEventPayload(
                orderPaymentOutboxMessage.getPayload(),
                OrderPaymentEventPayload.class
        );
        PaymentRequestAvroModel data = orderMessagingDataMapper.orderPaymentEventToPaymentRequestAvroModel(
                sagaId,
                payload
        );

        try{

            kafkaProducer.send(
                    topicName,
                    sagaId,
                    data,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getPaymentRequestTopicName(),
                            data,
                            orderPaymentOutboxMessage,
                            outboxCallback,
                            payload.getOrderId(),
                            "PaymentRequestAvroModel")
            );
            log.info("OrderPaymentEventPayload sent to Kafka for order id: {} and saga id: {}",
                    data.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderPaymentEventPayload" +
                            " to kafka with order id: {} and saga id: {}, error: {}",
                    data.getOrderId(), sagaId, e.getMessage());
        }
    }
}
