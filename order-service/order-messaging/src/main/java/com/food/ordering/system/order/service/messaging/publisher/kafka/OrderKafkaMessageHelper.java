package com.food.ordering.system.order.service.messaging.publisher.kafka;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderKafkaMessageHelper {

  public <T> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
      String responseTopicName, T requestAvroModel, String orderId, String requestAvroModelName) {

    return new ListenableFutureCallback<SendResult<String, T>>() {
      @Override
      public void onFailure(Throwable ex) {
        log.error("Error while sending {} message: {} to topic: {}",
            requestAvroModelName, requestAvroModel.toString(), responseTopicName, ex);
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
      }
    };
  }
}
