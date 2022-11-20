package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxCleanerScheduler implements OutboxScheduler {

  private final OrderOutboxHelper orderOutboxHelper;

  @Transactional
  @Scheduled(cron = "@midnight")
  @Override
  public void processOutboxMessage() {
    Optional<List<OrderOutboxMessage>> outboxMessageResponse = orderOutboxHelper
        .getOrderOutboxMessageByOutboxStatus(OutboxStatus.STARTED);
    if (outboxMessageResponse.isPresent() && !outboxMessageResponse.get().isEmpty()) {
      List<OrderOutboxMessage> outboxMessages = outboxMessageResponse.get();
      log.info("Received {} OrderOutboxMessage for clean-up!", outboxMessages.size());
      orderOutboxHelper.deleteOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
      log.info("Deleted {} OrderOutboxMessage!", outboxMessages.size());
    }
  }
}
