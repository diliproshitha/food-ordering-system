package com.food.ordering.system.order.service.dataaccess.outbox.payment.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.dataaccess.outbox.payment.exception.PaymentOutboxNotFoundException;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.mapper.PaymentOutboxDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

  private final PaymentOutboxJpaRepository paymentOutboxJpaRepository;
  private final PaymentOutboxDataAccessMapper paymentOutboxDataAccessMapper;

  @Override
  public OrderPaymentOutboxMessage save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
    return paymentOutboxDataAccessMapper
        .paymentOutboxEntityToOrderPaymentOutboxMessage(paymentOutboxJpaRepository
            .save(paymentOutboxDataAccessMapper
                .orderPaymentOutboxMessageToOutboxEntity(orderPaymentOutboxMessage)));
  }

  @Override
  public Optional<List<OrderPaymentOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String sagaType,
      OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
    return Optional.of(paymentOutboxJpaRepository.findByTypeAndOutboxStatusAndSagaStatusIn(sagaType,
        outboxStatus, Arrays.asList(sagaStatuses))
        .orElseThrow(() -> new PaymentOutboxNotFoundException("Payment outbox object " +
            "could not be found for saga type " + sagaType))
        .stream()
        .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage)
        .collect(Collectors.toList()));
  }

  @Override
  public Optional<OrderPaymentOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type,
      UUID sagaId, SagaStatus... sagaStatuses) {
    return paymentOutboxJpaRepository
        .findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatuses))
        .map(paymentOutboxDataAccessMapper::paymentOutboxEntityToOrderPaymentOutboxMessage);
  }

  @Override
  public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus,
      SagaStatus... sagaStatuses) {
    paymentOutboxJpaRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus,
        Arrays.asList(sagaStatuses));
  }
}
