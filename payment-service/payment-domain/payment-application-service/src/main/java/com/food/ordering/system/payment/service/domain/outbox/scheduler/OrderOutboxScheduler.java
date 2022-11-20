package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.port.output.message.publisher.PaymentResponseMessagePublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxScheduler implements OutboxScheduler {

  private final OrderOutboxHelper orderOutboxHelper;
  private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;

  @Override
  @Transactional
  @Scheduled(fixedRateString = "${payment-service.outbox-scheduler-fixed-rate}",
  initialDelayString = "${payment-service.outbox-scheduler-initial-delay}")
  public void processOutboxMessage() {
    Optional<List<OrderOutboxMessage>> outboxMessagesOptional = orderOutboxHelper
        .getOrderOutboxMessageByOutboxStatus(OutboxStatus.STARTED);
    if (outboxMessagesOptional.isPresent() && outboxMessagesOptional.get().size() > 0) {
      List<OrderOutboxMessage> orderOutboxMessages = outboxMessagesOptional.get();
      log.info("Received {} OrderOutboxMessage with ids {}, sending to kafka!", orderOutboxMessages.size(),
          orderOutboxMessages.stream().map(outboxMessage ->
              outboxMessage.getId().toString()).collect(Collectors.joining(",")));
      orderOutboxMessages.forEach(orderOutboxMessage ->
          paymentResponseMessagePublisher.publish(orderOutboxMessage, orderOutboxHelper::updateOutboxMessage));
      log.info("{} OrderOutboxMessage sent to message bus!", orderOutboxMessages.size());
    }
  }

}
