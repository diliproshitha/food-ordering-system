package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.swing.text.html.Option;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.exception.PaymentDomainException;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.port.output.repository.OrderOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxHelper {

  private final OrderOutboxRepository orderOutboxRepository;
  private final ObjectMapper objectMapper;

  @Transactional(readOnly = true)
  public Optional<OrderOutboxMessage> getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(UUID sagaId,
      PaymentStatus paymentStatus) {
    return orderOutboxRepository.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(ORDER_SAGA_NAME, sagaId,
        paymentStatus, OutboxStatus.COMPLETED);
  }

  @Transactional(readOnly = true)
  public Optional<List<OrderOutboxMessage>> getOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
    return orderOutboxRepository.findByTypeAndOutboxStatus(ORDER_SAGA_NAME, outboxStatus);
  }

  @Transactional
  public void deleteOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
    orderOutboxRepository.deleteByTypeAndOutboxStatus(ORDER_SAGA_NAME, outboxStatus);
  }

  @Transactional
  public void saveOutboxMessage(OrderEventPayload orderEventPayload, PaymentStatus paymentStatus,
      OutboxStatus outboxStatus, UUID sagaId) {
    save(OrderOutboxMessage.builder()
        .id(UUID.randomUUID())
        .sagaId(sagaId)
        .createdAt(orderEventPayload.getCreatedAt())
        .processedAt(ZonedDateTime.now(ZoneId.of(UTC)))
        .type(ORDER_SAGA_NAME)
        .payload(createPayload(orderEventPayload))
        .paymentStatus(paymentStatus)
        .outboxStatus(outboxStatus)
        .build());
  }

  private String createPayload(OrderEventPayload orderEventPayload) {
    try {
      return objectMapper.writeValueAsString(orderEventPayload);
    } catch (JsonProcessingException e) {
      log.error("Could not create OrderEventPayload json!", e);
      throw new PaymentDomainException("Could not create OrderEventPayload json!", e);
    }
  }

  private void save(OrderOutboxMessage orderOutboxMessage) {
    OrderOutboxMessage response = orderOutboxRepository.save(orderOutboxMessage);

    if (Objects.isNull(response)) {
      log.error("Could not save OrderOutboxMessage!");
      throw new PaymentDomainException("Could not save OrderOutboxMessage!");
    }
    log.info("OrderOutboxMessage is saved with id: {}", orderOutboxMessage.getId());
  }

  @Transactional
  public void updateOutboxMessage(OrderOutboxMessage orderOutboxMessage, OutboxStatus outboxStatus) {
    orderOutboxMessage.setOutboxStatus(outboxStatus);
    save(orderOutboxMessage);
    log.info("Order outbox table status is updated as: {}", outboxStatus.name());
  }
}
