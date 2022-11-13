package com.food.ordering.system.order.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.exception.OrderNotFoundException;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.kafka.producer.KafkaConsumer;
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
      try {
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
      } catch (OptimisticLockingFailureException e) {
        // No operation should be done for optimistic lock. This means another thread has finished the work,
        // do not throw error to prevent reading the data from kafka again!
        log.error("Caught optimistic locking exception in PaymentResponseKafkaListener for order id: {}",
            paymentResponseAvroModel.getOrderId());
      } catch (OrderNotFoundException e) {
        // No-Op for OrderNotFoundException
        log.error("No order found for order id: {}", paymentResponseAvroModel.getOrderId());
      }
    });
  }
}
