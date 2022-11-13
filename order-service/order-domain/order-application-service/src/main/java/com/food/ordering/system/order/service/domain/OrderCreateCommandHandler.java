package com.food.ordering.system.order.service.domain;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateCommandHandler {

  private final OrderDataMapper orderDataMapper;
  private final OrderCreateHandler orderCreateHandler;
  private final PaymentOutboxHelper paymentOutboxHelper;
  private final OrderSagaHelper orderSagaHelper;

  @Transactional
  public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
    OrderCreatedEvent orderCreatedEvent = orderCreateHandler.persistOrder(createOrderCommand);
    CreateOrderResponse createOrderResponse = orderDataMapper.orderToCreateOrderResponse(
        orderCreatedEvent.getOrder(), "Order created successfully!");

    paymentOutboxHelper.savePaymentOutboxMessage(orderDataMapper.orderCreatedEventToOrderPaymentEventPayload(orderCreatedEvent),
        orderCreatedEvent.getOrder().getOrderStatus(),
        orderSagaHelper.orderStatusToSagaStatus(orderCreatedEvent.getOrder().getOrderStatus()),
        OutboxStatus.STARTED, UUID.randomUUID());

    log.info("Returning CreateOrderResponse with order id: {}", orderCreatedEvent.getOrder().getId());

    return createOrderResponse;
  }

}
