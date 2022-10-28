package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.order.service.domain.ports.inputs.message.listener.payment.PaymentResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentResponseKafkaListener implements KafkaConsumer<PaymentResponseAvroModel> {

  private final PaymentResponseMessageListener paymentResponseMessageListener;
  private final OrderMessagingDataMapper orderMessagingDataMapper;

  @Override
  @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
      topics = "${order-service.payment-response-topic-name}")
  public void receive(@Payload List<PaymentResponseAvroModel> messages,
      @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
      @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
    log.info("{} number of payment responses received with keys: {}, partitions: {} and offset: {}",
        messages.size(), keys.toString(), partitions.toString(), offsets.toString());

    messages.forEach(paymentResponseAvroModel -> {
      if (PaymentStatus.COMPLETED.name().equals(paymentResponseAvroModel.getPaymentOrderStatus().name())) {
        log.info("Processing successful payment for order id: {}", paymentResponseAvroModel.getOrderId());
        paymentResponseMessageListener.paymentCompleted(orderMessagingDataMapper
            .paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
      } else if (PaymentStatus.FAILED.name().equals(paymentResponseAvroModel.getPaymentOrderStatus().name()) ||
        PaymentStatus.CANCELLED.name().equals(paymentResponseAvroModel.getPaymentOrderStatus().name())) {
        log.info("Processing unsuccessful payment for order id: {}", paymentResponseAvroModel.getOrderId());
        paymentResponseMessageListener.paymentCancelled(orderMessagingDataMapper
            .paymentResponseAvroModelToPaymentResponse(paymentResponseAvroModel));
      }
    });
  }
}
