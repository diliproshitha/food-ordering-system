package com.food.ordering.system.payment.service.messaging.publisher.kafka;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.producer.service.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.port.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentCompletedKafkaMessagePublisher implements PaymentCompletedMessagePublisher {

  private final PaymentMessagingDataMapper paymentMessagingDataMapper;
  private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
  private final PaymentServiceConfigData paymentServiceConfigData;
  private final KafkaMessageHelper kafkaMessageHelper;

  @Override
  public void publish(PaymentCompletedEvent domainEvent) {
    String orderId = domainEvent.getPayment().getOrderId().getValue().toString();

    log.info("Received PaymentCompletedEvent for order id: {}" , orderId);

    try {
      PaymentResponseAvroModel paymentResponseAvroModel = paymentMessagingDataMapper
          .paymentCompletedEventToPaymentResponseAvroModel(domainEvent);
      kafkaProducer.send(paymentServiceConfigData.getPaymentResponseTopicName(),
          orderId,
          paymentResponseAvroModel,
          kafkaMessageHelper.getKafkaCallback(paymentServiceConfigData.getPaymentResponseTopicName(),
              paymentResponseAvroModel,
              orderId,
              PaymentResponseAvroModel.class.getName()));
    } catch (Exception e) {
      log.error("Error while sending PaymentResponseAvroModel message to kafka with order id: {}, "
          + "error: {}", orderId, e.getMessage());
    }
  }
}
