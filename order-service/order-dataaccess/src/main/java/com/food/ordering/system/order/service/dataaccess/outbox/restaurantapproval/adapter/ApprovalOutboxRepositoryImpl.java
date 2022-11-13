package com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.ApprovalOutboxJpaRepository;
import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.exception.ApprovalOutboxNotFoundException;
import com.food.ordering.system.order.service.dataaccess.outbox.restaurantapproval.mapper.ApprovalOutboxDataAccessMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApprovalOutboxRepositoryImpl implements ApprovalOutboxRepository {

  private final ApprovalOutboxJpaRepository approvalOutboxJpaRepository;
  private final ApprovalOutboxDataAccessMapper approvalOutboxDataAccessMapper;

  @Override
  public OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
    return approvalOutboxDataAccessMapper
        .approvalOutboxEntityToOrderApprovalOutboxMessage(approvalOutboxJpaRepository
            .save(approvalOutboxDataAccessMapper
                .orderCreatedOutboxMessageToOutboxEntity(orderApprovalOutboxMessage)));
  }

  @Override
  public Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(
      String sagaType, OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
    return Optional.of(approvalOutboxJpaRepository.findByTypeAndOutboxStatusAndSagaStatusIn(sagaType,
        outboxStatus, Arrays.asList(sagaStatuses))
        .orElseThrow(() -> new ApprovalOutboxNotFoundException("Approval outbox object " +
            "could be found for saga type " + sagaType))
        .stream()
        .map(approvalOutboxDataAccessMapper::approvalOutboxEntityToOrderApprovalOutboxMessage)
        .collect(Collectors.toList()));
  }

  @Override
  public Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type,
      UUID sagaId, SagaStatus... sagaStatuses) {
    return approvalOutboxJpaRepository
        .findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatuses))
        .map(approvalOutboxDataAccessMapper::approvalOutboxEntityToOrderApprovalOutboxMessage);
  }

  @Override
  public void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus,
      SagaStatus... sagaStatuses) {
    approvalOutboxJpaRepository.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus,
        Arrays.asList(sagaStatuses));
  }
}
