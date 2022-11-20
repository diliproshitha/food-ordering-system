package com.food.ordering.system.payment.service.messaging.listener.kafka;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaConsumer;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.port.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentListenerKafkaListener implements KafkaConsumer<PaymentRequestAvroModel> {

  private final PaymentRequestMessageListener paymentRequestMessageListener;
  private final PaymentMessagingDataMapper paymentMessagingDataMapper;

  @Override
  @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
      topics = "${payment-service.payment-request-topic-name}")
  public void receive(@Payload List<PaymentRequestAvroModel> messages,
      @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
      @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
    log.info("{} number of payment requests received with keys: {}, partitions: {} and offset: {}",
        messages.size(),
        keys.toString(),
        partitions.toString(),
        offsets.toString());
    messages.forEach(paymentRequestAvroModel -> {
      try {
        if (PaymentOrderStatus.PENDING.name().equals(paymentRequestAvroModel.getPaymentOrderStatus().name())) {
          log.info("Processing payment for order id: {}", paymentRequestAvroModel.getOrderId());
          paymentRequestMessageListener.completePayment(paymentMessagingDataMapper
              .paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel));
        } else if (PaymentOrderStatus.CANCELLED.name().equals(paymentRequestAvroModel.getPaymentOrderStatus().name())) {
          log.info("Cancelling payment for order id: {}", paymentRequestAvroModel.getOrderId());
          paymentRequestMessageListener.cancelPayment(paymentMessagingDataMapper
              .paymentRequestAvroModelToPaymentRequest(paymentRequestAvroModel));
        }
      } catch (DataAccessException e) {
        SQLException sqlException = (SQLException) e.getRootCause();
        if (Objects.nonNull(sqlException) && Objects.nonNull(sqlException.getSQLState()) &&
            PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {

          // NO-OP for unique constraint exception
          log.error("Caught unique constraint exception with sql state: {} "
              + "in PaymentListenerKafkaListener for order id: {}",
              sqlException.getSQLState(), paymentRequestAvroModel.getOrderId());
        } else {
          throw new PaymentApplicationServiceException("Throwing DataAccessException in "
              + "PaymentListenerKafkaListener: " + e.getMessage(), e);
        }
      } catch (PaymentNotFoundException e) {
        // NO-OP for PaymentNotFoundException
        log.error("No payment found for order id: {}", paymentRequestAvroModel.getOrderId());
      }
    });
  }
}
