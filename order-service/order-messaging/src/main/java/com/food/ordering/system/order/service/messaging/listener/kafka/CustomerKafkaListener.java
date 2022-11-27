package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;
import com.food.ordering.system.kafka.producer.KafkaConsumer;
import com.food.ordering.system.order.service.domain.ports.inputs.message.listener.customer.CustomerMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerKafkaListener implements KafkaConsumer<CustomerAvroModel> {

  private final CustomerMessageListener customerMessageListener;
  private final OrderMessagingDataMapper orderMessagingDataMapper;

  @Override
  @KafkaListener(id = "${kafka-consumer-config.customer-group-id}", topics = "${order-service.customer-topic-name}")
  public void receive(@Payload List<CustomerAvroModel> messages,
      @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
      @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

    log.info("{} number of customer create messages received with keys: {}, partitions: {} and offsets: {}",
        messages.size(), keys.toString(), partitions.toString(), offsets.toString());
    messages.forEach(customerAvroModel -> {
      customerMessageListener
          .customerCreated(orderMessagingDataMapper.customerAvroModelToCustomerModel(customerAvroModel));
    });
  }
}
