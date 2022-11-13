package com.food.ordering.system.order.service.domain.ports.outputs.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

public interface ApprovalOutboxRepository {

  OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage orderApprovalOutboxMessage);

  Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String type,
      OutboxStatus outboxStatus, SagaStatus... sagaStatuses);

  Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type,
      UUID sagaId, SagaStatus... sagaStatuses);

  void deleteByTypeAndOutboxStatusAndSagaStatus(String type, OutboxStatus outboxStatus, SagaStatus... sagaStatuses);

}
