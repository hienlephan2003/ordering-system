package org.ordering.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.ordering.order.service.domain.exception.OrderDomainException;
import org.ordering.outbox.OutboxStatus;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class KafkaMessageHelper {

    private final ObjectMapper objectMapper;

    public KafkaMessageHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T getOrderEventPayload(String payload, Class<T> outputType) {
        try {
            return objectMapper.readValue(payload, outputType);
        } catch (JsonProcessingException e) {
            log.error("Could not read {} object!", outputType.getName(), e);
            throw new OrderDomainException("Could not read " + outputType.getName() + " object!", e);
        }
    }
    public <T, U> CompletableFuture<SendResult<String, T>>
    getKafkaCallback(String responseTopicName, T avroModel, U outboxMessage,
                     BiConsumer<U, OutboxStatus> outboxCallback,
                     String orderId, String avroModelName) {
        return new CompletableFuture<SendResult<String, T>>().whenComplete((result, throwable) ->{
            if(throwable == null){
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Received successful response from Kafka for order id: {}" +
                                " Topic: {} Partition: {} Offset: {} Timestamp: {}",
                        orderId,
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp());
                outboxCallback.accept(outboxMessage, OutboxStatus.COMPLETED);
            }
            else{
                log.error("Error while sending {} with message: {} and outbox type: {} to topic {}",
                        avroModelName, avroModel.toString(), outboxMessage.getClass().getName(), responseTopicName, throwable);
                outboxCallback.accept(outboxMessage, OutboxStatus.FAILED);
            }
        });
    }
}
