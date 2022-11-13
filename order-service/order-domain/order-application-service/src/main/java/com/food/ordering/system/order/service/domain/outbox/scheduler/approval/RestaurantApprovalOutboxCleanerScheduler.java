package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalOutboxCleanerScheduler implements OutboxScheduler {

  private final ApprovalOutboxHelper approvalOutboxHelper;

  @Override
  @Scheduled(cron = "@midnight")
  public void processOutboxMessage() {
    Optional<List<OrderApprovalOutboxMessage>> response = approvalOutboxHelper
        .getApprovalOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED,
            SagaStatus.SUCCEEDED, SagaStatus.FAILED, SagaStatus.COMPENSATED);

    if (response.isPresent()) {
      List<OrderApprovalOutboxMessage> outboxMessages = response.get();
      log.info("Received {} OrderApprovalOutboxMessage for clean-up. The payloads: {}",
          outboxMessages.size(),
          outboxMessages.stream().map(OrderApprovalOutboxMessage::getPayload)
              .collect(Collectors.joining(",")));

      approvalOutboxHelper.deleteApprovalOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED,
          SagaStatus.SUCCEEDED, SagaStatus.FAILED, SagaStatus.COMPENSATED);
      log.info("{} OrderApprovalOutboxMessage deleted!", outboxMessages.size());
    }
  }
}
