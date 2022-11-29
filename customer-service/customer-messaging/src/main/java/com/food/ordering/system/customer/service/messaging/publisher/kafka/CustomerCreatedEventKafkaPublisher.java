package com.food.ordering.system.customer.service.messaging.publisher.kafka;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.food.ordering.system.customer.service.domain.config.CustomerServiceConfigData;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.customer.service.domain.ports.output.message.publisher.CustomerMessagePublisher;
import com.food.ordering.system.customer.service.messaging.mapper.CustomerMessagingDataMapper;
import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.google.common.util.concurrent.ListenableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerCreatedEventKafkaPublisher implements CustomerMessagePublisher {

  private final CustomerMessagingDataMapper customerMessagingDataMapper;
  private final KafkaProducer<String, CustomerAvroModel> kafkaProducer;
  private final CustomerServiceConfigData customerServiceConfigData;


  @Override
  public void publish(CustomerCreatedEvent customerCreatedEvent) {
    log.info("Received CustomerCreatedEvent for customer id: {}",
        customerCreatedEvent.getCustomer().getId().getValue());
    try {
      CustomerAvroModel customerAvroModel = customerMessagingDataMapper
          .paymentResponseAvroModelToPaymentResponse(customerCreatedEvent);

      kafkaProducer.send(customerServiceConfigData.getCustomerTopicName(), customerAvroModel.getId(),
          customerAvroModel, getCallback(customerServiceConfigData.getCustomerTopicName(), customerAvroModel));

    } catch (Exception e) {
      log.error("Error while sending CustomerCreatedEvent to kafka for customer id: {}, error: {}",
          customerCreatedEvent.getCustomer().getId().getValue(), e.getMessage());
    }
  }

  private ListenableFutureCallback<SendResult<String, CustomerAvroModel>> getCallback(String topicName,
      CustomerAvroModel message) {
    return new ListenableFutureCallback<>() {
      @Override
      public void onFailure(Throwable ex) {
        log.error("Error while sending message {} to topic {}", message.toString(), topicName, ex);
      }

      @Override
      public void onSuccess(SendResult<String, CustomerAvroModel> result) {
        RecordMetadata recordMetadata = result.getRecordMetadata();
        log.info("Received new metadata. Topic: {}; Partition {}; Offset {}; Timestamp {}, at time {}",
            recordMetadata.topic(),
            recordMetadata.partition(),
            recordMetadata.offset(),
            recordMetadata.timestamp(),
            System.nanoTime());
      }
    };
  }
}
