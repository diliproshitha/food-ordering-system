package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.outputs.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxScheduler implements OutboxScheduler {

  private final PaymentOutboxHelper paymentOutboxHelper;
  private final PaymentRequestMessagePublisher paymentRequestMessagePublisher;

  @Override
  @Transactional
  @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
  initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
  public void processOutboxMessage() {

    Optional<List<OrderPaymentOutboxMessage>> outboxMessageResponse = paymentOutboxHelper.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
        OutboxStatus.STARTED, SagaStatus.STARTED, SagaStatus.COMPENSATING);

    if (outboxMessageResponse.isPresent() && outboxMessageResponse.get().size() > 0) {
      List<OrderPaymentOutboxMessage> orderPaymentOutboxMessages = outboxMessageResponse.get();
      log.info("Received {} OrderPaymentOutboxMessage with ids: {}, sending to message bus!",
          orderPaymentOutboxMessages.size(),
          orderPaymentOutboxMessages.stream().map(orderPaymentOutboxMessage ->
              orderPaymentOutboxMessage.getId().toString()).collect(Collectors.joining(",")));
      orderPaymentOutboxMessages.forEach(orderPaymentOutboxMessage ->
          paymentRequestMessagePublisher.publish(orderPaymentOutboxMessage, this::updateOutboxStatus));
      log.info("{} OrderPaymentOutboxMessage sent to message bus!", orderPaymentOutboxMessages.size());
    }

  }

  @Transactional
  private void updateOutboxStatus(OrderPaymentOutboxMessage outboxMessage, OutboxStatus outboxStatus) {
    outboxMessage.setOutboxStatus(outboxStatus);
    paymentOutboxHelper.save(outboxMessage);
    log.info("OrderPaymentOutboxMessage saved with outbox id: {}", outboxMessage.getId());
  }
}
