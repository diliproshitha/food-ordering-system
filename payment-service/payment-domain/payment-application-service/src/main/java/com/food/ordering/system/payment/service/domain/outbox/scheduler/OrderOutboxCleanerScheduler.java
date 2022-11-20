package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxCleanerScheduler implements OutboxScheduler {

  private final OrderOutboxHelper orderOutboxHelper;

  @Override
  @Transactional
  @Scheduled(cron = "@midnight")
  public void processOutboxMessage() {
    Optional<List<OrderOutboxMessage>> outboxMessagesOptional = orderOutboxHelper
        .getOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);

    if (outboxMessagesOptional.isPresent() && outboxMessagesOptional.get().size() > 0) {
      List<OrderOutboxMessage> orderOutboxMessages = outboxMessagesOptional.get();
      log.info("Received {} OrderOutboxMessage for clean-up!", orderOutboxMessages.size());
      orderOutboxHelper.deleteOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
      log.info("Deleted {} OrderOutboxMessage!", orderOutboxMessages.size());
    }
  }
}
