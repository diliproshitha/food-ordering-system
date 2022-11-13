package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.dto.create.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.inputs.message.listener.payment.PaymentResponseMessageListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

  private final OrderPaymentSaga orderPaymentSaga;

  @Override
  public void paymentCompleted(PaymentResponse paymentResponse) {
    orderPaymentSaga.process(paymentResponse);
    log.info("Order payment Saga process operation is completed for order id: {}", paymentResponse.getOrderId());
  }

  @Override
  public void paymentCancelled(PaymentResponse paymentResponse) {
    orderPaymentSaga.rollback(paymentResponse);
    log.info("Order is roll backed for order id: {} with failure messages: {}",
        paymentResponse.getOrderId(),
        String.join(FAILURE_MESSAGE_DELIMITER, paymentResponse.getFailureMessages()));
  }
}
