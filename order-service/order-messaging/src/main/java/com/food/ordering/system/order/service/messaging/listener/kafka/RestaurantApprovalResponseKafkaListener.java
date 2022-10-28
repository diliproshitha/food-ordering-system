package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.ports.inputs.message.listener.restaurantapproval.RestaurantApprovalResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestaurantApprovalResponseKafkaListener implements
    KafkaConsumer<RestaurantApprovalResponseAvroModel> {

  private final RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener;
  private final OrderMessagingDataMapper orderMessagingDataMapper;

  @Override
  @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
      topics = "${order-service.restaurant-approval-response-topic-name}")
  public void receive(@Payload List<RestaurantApprovalResponseAvroModel> messages,
      @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
      @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
    log.info("{} number of payment responses received with keys: {}, partitions: {} and offset: {}",
        messages.size(), keys.toString(), partitions.toString(), offsets.toString());
    messages.forEach(restaurantApprovalResponseAvroModel -> {
      if (OrderApprovalStatus.APPROVED.name().equals(restaurantApprovalResponseAvroModel
          .getOrderApprovalStatus().name())) {
        log.info("Processing approved order for order id: {}", restaurantApprovalResponseAvroModel
            .getOrderId());
        restaurantApprovalResponseMessageListener.orderApproved(orderMessagingDataMapper.
            approvalResponseAvroModelToApprovalResponse(restaurantApprovalResponseAvroModel));
      }
      else if (OrderApprovalStatus.REJECTED.name().equals(restaurantApprovalResponseAvroModel
          .getOrderApprovalStatus().name())) {
        log.info("Processing rejected order for order id: {}, with failure messages: {}",
            restaurantApprovalResponseAvroModel.getOrderId(),
            String.join(Order.FAILURE_MESSAGE_DELIMITER, restaurantApprovalResponseAvroModel.getFailureMessages()));
        restaurantApprovalResponseMessageListener.orderRejected(orderMessagingDataMapper
            .approvalResponseAvroModelToApprovalResponse(restaurantApprovalResponseAvroModel));
      }
    });
  }
}
