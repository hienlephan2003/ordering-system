package org.ordering.kafka.producer.service.impl;

import com.google.common.util.concurrent.ListenableFuture;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.ordering.kafka.producer.service.KafkaProducer;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.security.auth.callback.Callback;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {
    private final KafkaTemplate<K,V> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    @Override
    public void send(String topicName, K key, V message, CompletableFuture<SendResult<K, V>> callback) {
        log.info("Sending message={} to topic={}", message, topicName);
            CompletableFuture<SendResult<K, V>> kafkaResultFuture = kafkaTemplate.send(topicName, key, message);
            kafkaResultFuture.whenComplete(((kvSendResult, throwable) -> {
                if(throwable == null){
                    callback.complete(kvSendResult);
                }
                else{
                    log.error("Error on kafka producer with key: {}, message: {} and exception: {}", key, message,
                            throwable.getMessage());
                    throw new KafkaProducerException(kvSendResult.getProducerRecord(),
                            "Error on kafka producer with key: " + key + " and message: " + message,
                            throwable
                            );
                }
            }));
        }
    @PreDestroy
    public void close(){
        if(kafkaTemplate != null){
            kafkaTemplate.destroy();
        }
    }

}
