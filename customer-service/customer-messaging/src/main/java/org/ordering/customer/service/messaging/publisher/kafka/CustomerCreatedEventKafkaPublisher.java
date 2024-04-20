package org.ordering.customer.service.messaging.publisher.kafka;
import org.ordering.customer.service.domain.config.CustomerServiceConfigData;
import org.ordering.customer.service.domain.event.CustomerCreatedEvent;
import org.ordering.customer.service.domain.ports.output.message.publisher.CustomerMessagePublisher;
import org.ordering.kafka.order.avro.model.CustomerAvroModel;
import org.ordering.kafka.producer.KafkaMessageHelper;
import org.ordering.kafka.producer.service.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.ordering.customer.service.messaging.publisher.kafka.mapper.CustomerMessagingDataMapper;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class CustomerCreatedEventKafkaPublisher implements CustomerMessagePublisher {

    private final CustomerMessagingDataMapper customerMessagingDataMapper;

    private final KafkaProducer<String, CustomerAvroModel> kafkaProducer;

    private final CustomerServiceConfigData customerServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;


    public CustomerCreatedEventKafkaPublisher(CustomerMessagingDataMapper customerMessagingDataMapper,
                                              KafkaProducer<String, CustomerAvroModel> kafkaProducer,
                                              CustomerServiceConfigData customerServiceConfigData, KafkaMessageHelper kafkaMessageHelper) {
        this.customerMessagingDataMapper = customerMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.customerServiceConfigData = customerServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(CustomerCreatedEvent customerCreatedEvent) {
        log.info("Received CustomerCreatedEvent for customer id: {}",
                customerCreatedEvent.getCustomer().getId().getValue());
        try {
            CustomerAvroModel customerAvroModel = customerMessagingDataMapper
                    .paymentResponseAvroModelToPaymentResponse(customerCreatedEvent);

            kafkaProducer.send(customerServiceConfigData.getCustomerTopicName(), customerAvroModel.getId().toString(),
                    customerAvroModel,
                    getCallback(customerServiceConfigData.getCustomerTopicName(), customerAvroModel));

            log.info("CustomerCreatedEvent sent to kafka for customer id: {}",
                    customerAvroModel.getId());
        } catch (Exception e) {
            log.error("Error while sending CustomerCreatedEvent to kafka for customer id: {}," +
                    " error: {}", customerCreatedEvent.getCustomer().getId().getValue(), e.getMessage());
        }
    }

    private CompletableFuture<SendResult<String, CustomerAvroModel>>
    getCallback(String topicName, CustomerAvroModel message) {
        return new CompletableFuture<SendResult<String, CustomerAvroModel>>().whenComplete((result, throwable) ->{
            if(throwable == null){
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Received new metadata. Topic: {}; Partition {}; Offset {}; Timestamp {}, at time {}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        System.nanoTime());
            }
            else{
                log.error("Can not send message {} to topic {}", message.toString(), topicName, throwable);
            }
           });
        }
}
