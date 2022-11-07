package com.food.ordering.system.restaurant.service.messaging.publisher.kafka;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderRejectedMessagePublisher;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRejectedKafkaMessagePublisher implements OrderRejectedMessagePublisher {

  private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
  private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer;
  private final RestaurantServiceConfigData restaurantServiceConfigData;
  private final KafkaMessageHelper kafkaMessageHelper;

  @Override
  public void publish(OrderRejectedEvent orderRejectedEvent) {
    String orderId = orderRejectedEvent.getOrderApproval().getOrderId().getValue().toString();

    log.info("Received OrderApprovedEvent for order id: {}", orderId);

    try {
      RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel =
          restaurantMessagingDataMapper
              .orderRejectedEventToRestaurantApprovalResponseAvroModel(orderRejectedEvent);

      kafkaProducer.send(restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
          orderId, restaurantApprovalResponseAvroModel, kafkaMessageHelper.getKafkaCallback(
              restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
              restaurantApprovalResponseAvroModel, orderId, RestaurantApprovalResponseAvroModel.class.getName()
          ));

      log.info("RestaurantApprovalResponseAvroModel sent to kafka at: {}", System.nanoTime());
    } catch (Exception ex) {
      log.error("Error while sending RestaurantApprovalResponseAvroModel message to kafka with order "
              + "id: {}, error: {}", orderId, ex.getMessage());

    }
  }
}