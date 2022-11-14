package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.outputs.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalOutboxScheduler implements OutboxScheduler {

  private final ApprovalOutboxHelper approvalOutboxHelper;
  private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;

  @Override
  @Transactional
  @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
      initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
  public void processOutboxMessage() {
    Optional<List<OrderApprovalOutboxMessage>> approvalMessagesOptional = approvalOutboxHelper.getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
        OutboxStatus.STARTED, SagaStatus.PROCESSING);

    if (approvalMessagesOptional.isPresent() && approvalMessagesOptional.get().size() > 0) {
      List<OrderApprovalOutboxMessage> outboxMessages = approvalMessagesOptional.get();
      log.info("Received {} OrderApprovalOutboxMessage with ids: {}, sending to message bus!",
          outboxMessages.size(), outboxMessages.stream().map(orderApprovalOutboxMessage ->
              orderApprovalOutboxMessage.getId().toString()).collect(Collectors.joining(",")));

      outboxMessages.forEach(outboxMessage ->
          restaurantApprovalRequestMessagePublisher.publish(outboxMessage, this::updateOutboxStatus));
      log.info("{} OrderApprovalOutboxMessage sent to message bus!", outboxMessages.size());
    }
  }

  private void updateOutboxStatus(OrderApprovalOutboxMessage orderApprovalOutboxMessage, OutboxStatus outboxStatus) {
    orderApprovalOutboxMessage.setOutboxStatus(outboxStatus);
    approvalOutboxHelper.save(orderApprovalOutboxMessage);
    log.info("OrderApprovalOutboxMessage is updated with outbox status: {}", outboxStatus.name());
  }
}
