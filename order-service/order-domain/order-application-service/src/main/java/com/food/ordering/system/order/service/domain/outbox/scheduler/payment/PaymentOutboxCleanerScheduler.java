package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {

  private final PaymentOutboxHelper paymentOutboxHelper;

  @Override
  @Scheduled(cron = "@midnight")
  public void processOutboxMessage() {
    Optional<List<OrderPaymentOutboxMessage>> outboxMessagesResponse = paymentOutboxHelper
        .getPaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED,
            SagaStatus.SUCCEEDED, SagaStatus.FAILED, SagaStatus.COMPENSATED);

    if (outboxMessagesResponse.isPresent()) {
      List<OrderPaymentOutboxMessage> orderPaymentOutboxMessages = outboxMessagesResponse.get();
      log.info("Received {} OrderPaymentOutboxMessages for clean-up. The payloads: {}",
          orderPaymentOutboxMessages.size(),
          orderPaymentOutboxMessages.stream().map(OrderPaymentOutboxMessage::getPayload)
              .collect(Collectors.joining("\n")));
      paymentOutboxHelper.deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(OutboxStatus.COMPLETED,
          SagaStatus.SUCCEEDED, SagaStatus.FAILED, SagaStatus.COMPENSATED);
      log.info("{} OrderPaymentOutboxMessage deleted!", orderPaymentOutboxMessages.size());
    }

  }
}
