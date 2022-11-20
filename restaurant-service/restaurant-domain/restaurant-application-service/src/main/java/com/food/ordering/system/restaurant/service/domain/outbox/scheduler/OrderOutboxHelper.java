package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import static com.food.ordering.system.domain.DomainConstants.UTC;
import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantDomainException;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderOutboxHelper {

  private final OrderOutboxRepository orderOutboxRepository;
  private final ObjectMapper objectMapper;

  @Transactional(readOnly = true)
  public Optional<OrderOutboxMessage> getCompletedOrderOutboxMessageBySagaIdAndOutboxStatus(UUID sagaId,
      OutboxStatus outboxStatus) {
    return orderOutboxRepository.findByTypeAndSagaIdAndOutboxStatus(ORDER_SAGA_NAME, sagaId, outboxStatus);
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
  public void saveOrderOutboxMessage(OrderEventPayload orderEventPayload, OrderApprovalStatus orderApprovalStatus,
      OutboxStatus outboxStatus, UUID sagaId) {
    save(OrderOutboxMessage.builder()
        .id(UUID.randomUUID())
        .sagaId(sagaId)
        .createdAt(orderEventPayload.getCreatedAt())
        .processedAt(ZonedDateTime.now(ZoneId.of(UTC)))
        .type(ORDER_SAGA_NAME)
        .payload(createPayload(orderEventPayload))
        .orderApprovalStatus(orderApprovalStatus)
        .outboxStatus(outboxStatus)
        .build());
  }

  @Transactional
  public void updateOutboxStatus(OrderOutboxMessage orderPaymentOutboxMessage, OutboxStatus outboxStatus) {
    orderPaymentOutboxMessage.setOutboxStatus(outboxStatus);
    save(orderPaymentOutboxMessage);
    log.info("Order outbox table status is updated as: {}", outboxStatus.name());
  }

  private void save(OrderOutboxMessage orderOutboxMessage) {
    OrderOutboxMessage response = orderOutboxRepository.save(orderOutboxMessage);

    if (Objects.isNull(response)) {
      throw new RestaurantDomainException("Could not save OrderOutboxMessage!");
    }
    log.info("OrderOutboxMessage saved with id: {}", orderOutboxMessage.getId());
  }

  private String createPayload(OrderEventPayload orderEventPayload) {
    try {
      return objectMapper.writeValueAsString(orderEventPayload);
    } catch (JsonProcessingException e) {
      log.error("Could not create OrderEventPayload json!", e);
      throw new RestaurantDomainException("Could not create OrderEventPayload json!", e);
    }
  }


}
