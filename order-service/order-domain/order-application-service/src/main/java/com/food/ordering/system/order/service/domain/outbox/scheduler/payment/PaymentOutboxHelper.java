package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxHelper {

  private final PaymentOutboxRepository paymentOutboxRepository;
  private final ObjectMapper objectMapper;

  @Transactional(readOnly = true)
  public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
      OutboxStatus  outboxStatus, SagaStatus... sagaStatuses) {
    return paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME,
        outboxStatus, sagaStatuses);
  }

  @Transactional(readOnly = true)
  public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID sagaId,
      SagaStatus... sagaStatuses) {
    return paymentOutboxRepository.findByTypeAndSagaIdAndSagaStatus(ORDER_SAGA_NAME, sagaId, sagaStatuses);
  }

  @Transactional
  public void save(OrderPaymentOutboxMessage paymentOutboxMessage) {
    OrderPaymentOutboxMessage response = paymentOutboxRepository.save(paymentOutboxMessage);
    if (Objects.isNull(response)) {
      log.error("Could not save OrderPaymentOutboxMessage with outbox id: {}",
          paymentOutboxMessage.getId());
      throw new OrderDomainException("Could not save OrderPaymentOutboxMessage with outbox id: " +
          paymentOutboxMessage.getId());
    }
    log.info("OrderPaymentOutboxMessage saved with outbox id: {}", paymentOutboxMessage.getId());
  }

  @Transactional
  public void savePaymentOutboxMessage(OrderPaymentEventPayload orderPaymentEventPayload,
      OrderStatus orderStatus, SagaStatus sagaStatus, OutboxStatus outboxStatus, UUID sagaId) {
    save(OrderPaymentOutboxMessage.builder()
        .id(UUID.randomUUID())
        .sagaId(sagaId)
        .createdAt(orderPaymentEventPayload.getCreatedAt())
        .type(ORDER_SAGA_NAME)
        .payload(createPayload(orderPaymentEventPayload))
        .orderStatus(orderStatus)
        .sagaStatus(sagaStatus)
        .outboxStatus(outboxStatus)
        .build());
  }

  @Transactional
  public void deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
    paymentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
  }

  private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
    try {
      return objectMapper.writeValueAsString(orderPaymentEventPayload);
    } catch (JsonProcessingException e) {
      log.error("Could not create OrderPaymentEventPayload object for order id: {}",
          orderPaymentEventPayload.getOrderId(), e);
      throw new OrderDomainException("Could not create OrderPaymentEventPayload object for order id: " +
          orderPaymentEventPayload.getOrderId(), e);
    }
  }
}
