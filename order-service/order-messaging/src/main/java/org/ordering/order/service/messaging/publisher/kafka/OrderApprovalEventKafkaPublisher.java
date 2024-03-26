package org.ordering.order.service.messaging.publisher.kafka;

import lombok.extern.slf4j.Slf4j;
import org.ordering.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import org.ordering.kafka.producer.KafkaMessageHelper;
import org.ordering.kafka.producer.service.KafkaProducer;
import org.ordering.order.service.domain.config.OrderServiceConfigData;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import org.ordering.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import org.ordering.order.service.domain.ports.output.mesage.publisher.approval.ApprovalRestaurantRequestMessagePublisher;
import org.ordering.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.ordering.outbox.OutboxStatus;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;
@Slf4j
@Component
public class OrderApprovalEventKafkaPublisher implements ApprovalRestaurantRequestMessagePublisher {
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    private final OrderServiceConfigData orderServiceConfigData;
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaMessageHelper kafkaMessageHelper;

    public OrderApprovalEventKafkaPublisher(KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer, OrderServiceConfigData orderServiceConfigData, OrderMessagingDataMapper orderMessagingDataMapper, KafkaMessageHelper kafkaMessageHelper) {
        this.kafkaProducer = kafkaProducer;
        this.orderServiceConfigData = orderServiceConfigData;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage, BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback) {
        String topic = orderServiceConfigData.getRestaurantApprovalRequestTopicName();
        String sagaId = orderApprovalOutboxMessage.getSagaId().toString();
        OrderApprovalEventPayload orderApprovalEventPayload =
                kafkaMessageHelper.getOrderEventPayload(orderApprovalOutboxMessage.getPayload(),
                        OrderApprovalEventPayload.class);

        RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel
                = orderMessagingDataMapper.orderApprovalEventToRestaurantApprovalRequestAvroModel(sagaId, orderApprovalEventPayload);
        try{
            kafkaProducer.send(topic, sagaId, restaurantApprovalRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                            restaurantApprovalRequestAvroModel,
                            orderApprovalOutboxMessage,
                            outboxCallback,
                            orderApprovalEventPayload.getOrderId(),
                            "RestaurantApprovalRequestAvroModel"));
            log.info("OrderApprovalEventPayload sent to kafka for order id: {} and saga id: {}",
                    restaurantApprovalRequestAvroModel.getOrderId(), sagaId);

        }
        catch (Exception err){
            log.error("Error while sending OrderApprovalEventPayload to kafka for order id: {} and saga id: {}," +
                    " error: {}", orderApprovalEventPayload.getOrderId(), sagaId, err.getMessage());

        }
    }
}
