package com.food.ordering.system.order.service.messaging.publisher.kafka;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaMessageHelper;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.outputs.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CancelOrderKafkaMessagePublisher implements
    OrderCancelledPaymentRequestMessagePublisher {

  private final OrderMessagingDataMapper orderMessagingDataMapper;
  private final OrderServiceConfigData orderServiceConfigData;
  private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
  private final KafkaMessageHelper kafkaMessageHelper;

  @Override
  public void publish(OrderCancelledEvent domainEvent) {
    String orderId = domainEvent.getOrder().getId().getValue().toString();
    log.info("Received OrderCancelledEvent for order id: {}", orderId);

    try {
      PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper
          .orderCancelledEventToPaymentRequestAvroModel(domainEvent);
      kafkaProducer.send(orderServiceConfigData.getPaymentRequestTopicName(),
          orderId,
          paymentRequestAvroModel,
          kafkaMessageHelper
              .getKafkaCallback(orderServiceConfigData
                  .getPaymentResponseTopicName(), paymentRequestAvroModel, orderId,
                  PaymentRequestAvroModel.class.getName()));
      log.info("PaymentRequestAvroModel sent to kafka for order id: {}",
          paymentRequestAvroModel.getOrderId());
    } catch (Exception e) {
      log.error("Error while sending PaymentRequestAvroModel message to kafka with order id: {}, error: {}",
          orderId, e.getMessage());
    }
  }
}
