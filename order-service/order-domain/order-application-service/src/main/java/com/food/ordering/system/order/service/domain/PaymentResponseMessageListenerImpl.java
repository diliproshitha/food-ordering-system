package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.ports.inputs.message.listener.payment.PaymentResponseMessageListener;

public class PaymentResponseMessageListenerImpl implements PaymentResponseMessageListener {

  @Override
  public void paymentCompleted(PaymentResponse paymentResponse) {

  }

  @Override
  public void paymentCancelled(PaymentResponse paymentResponse) {

  }
}
