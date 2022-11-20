package com.food.ordering.system.restaurant.service.dataaccess.restaurant.outbox.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.outbox.exception.OrderOutboxNotFoundException;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.outbox.mapper.OrderOutboxDataAccessMapper;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.outbox.repository.OrderOutboxJpaRepository;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderOutboxRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {

  private final OrderOutboxJpaRepository orderOutboxJpaRepository;
  private final OrderOutboxDataAccessMapper orderOutboxDataAccessMapper;

  @Override
  public OrderOutboxMessage save(OrderOutboxMessage orderPaymentOutboxMessage) {
    return orderOutboxDataAccessMapper
        .orderOutboxEntityToOrderOutboxMessage(orderOutboxJpaRepository
            .save(orderOutboxDataAccessMapper
                .orderOutboxMessageToOutboxEntity(orderPaymentOutboxMessage)));
  }

  @Override
  public Optional<List<OrderOutboxMessage>> findByTypeAndOutboxStatus(String sagaType,
      OutboxStatus outboxStatus) {
    return Optional.of(orderOutboxJpaRepository.findByTypeAndOutboxStatus(sagaType, outboxStatus)
        .orElseThrow(() -> new OrderOutboxNotFoundException("Approval outbox object cannot be found for saga type " + sagaType))
        .stream()
        .map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage)
        .collect(Collectors.toList()));
  }

  @Override
  public Optional<OrderOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(String type, UUID sagaId,
      OutboxStatus outboxStatus) {
    return orderOutboxJpaRepository.findByTypeAndSagaIdAndOutboxStatus(type, sagaId, outboxStatus)
        .map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage);
  }

  @Override
  public void deleteByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus) {
    orderOutboxJpaRepository.deleteByTypeAndOutboxStatus(type, outboxStatus);
  }
}
