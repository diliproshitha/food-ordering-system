package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxScheduler implements OutboxScheduler {

  private final OrderOutboxHelper orderOutboxHelper;
  private final RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher;

  @Override
  @Transactional
  @Scheduled(fixedRateString = "${restaurant-service.outbox-scheduler-fixed-rate}",
  initialDelayString = "${restaurant-service.outbox-scheduler-initial-delay}")
  public void processOutboxMessage() {

    Optional<List<OrderOutboxMessage>> outboxMessageResponse = orderOutboxHelper
        .getOrderOutboxMessageByOutboxStatus(OutboxStatus.STARTED);
    if (outboxMessageResponse.isPresent() && !outboxMessageResponse.get().isEmpty()) {
      List<OrderOutboxMessage> orderOutboxMessages = outboxMessageResponse.get();
      log.info("Received {} OrderOutboxMessage with ids {}, sending to message bus!", orderOutboxMessages.size(),
          orderOutboxMessages.stream().map(outboxMessage ->
              outboxMessage.getId().toString()).collect(Collectors.joining(",")));

      orderOutboxMessages.forEach(orderOutboxMessage -> restaurantApprovalResponseMessagePublisher
          .publish(orderOutboxMessage, orderOutboxHelper::updateOutboxStatus));

      log.info("{} OrderOutboxMessage sent to message bus!", orderOutboxMessages.size());
    }

  }
}
