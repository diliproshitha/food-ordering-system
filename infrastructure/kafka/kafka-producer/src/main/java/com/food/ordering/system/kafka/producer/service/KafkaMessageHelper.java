package com.food.ordering.system.kafka.producer.service;

import java.util.function.BiConsumer;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.exception.DomainException;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageHelper {

  private final ObjectMapper objectMapper;

  public <T, U> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
      String responseTopicName, T avroModel, U outboxMessage,
      BiConsumer<U, OutboxStatus> outboxCallBack,
      String orderId, String avroModelName) {

    return new ListenableFutureCallback<SendResult<String, T>>() {
      @Override
      public void onFailure(Throwable ex) {
        log.error("Error while sending {} with message: {} and outbox type: {} to topic: {}",
            avroModelName, avroModel.toString(), outboxMessage.getClass().getName(), responseTopicName, ex);
        outboxCallBack.accept(outboxMessage, OutboxStatus.FAILED);
      }

      @Override
      public void onSuccess(SendResult<String, T> result) {
        RecordMetadata recordMetadata = result.getRecordMetadata();
        log.info("Received successful response from kafka for order id: {}, Topic: {}, Partition: {}, "
                + "Offset: {}, Timestamp: {}",
            orderId,
            recordMetadata.topic(),
            recordMetadata.partition(),
            recordMetadata.offset(),
            recordMetadata.timestamp());
        outboxCallBack.accept(outboxMessage, OutboxStatus.COMPLETED);
      }
    };
  }



  public <T> T getOrderEventPayload(String payload, Class<T> outputType) {
    try {
      return objectMapper.readValue(payload, outputType);
    } catch (JsonProcessingException e) {
      log.error("Could not read {} object!", outputType.getName(), e);
      throw new DomainException("Could not read " + outputType.getName() + " object!", e);
    }
  }
}
