package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.service.domain.dto.create.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {

  private final OrderDomainService orderDomainService;
  private final OrderSagaHelper orderSagaHelper;
  private PaymentOutboxHelper paymentOutboxHelper;
  private OrderDataMapper orderDataMapper;
  private ApprovalOutboxHelper approvalOutboxHelper;

  @Override
  @Transactional
  public void process(RestaurantApprovalResponse restaurantApprovalResponse) {

    Optional<OrderApprovalOutboxMessage> outboxMessageOptional = approvalOutboxHelper
        .getApprovalOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(restaurantApprovalResponse.getSagaId()
        ), SagaStatus.PROCESSING);

    if (outboxMessageOptional.isEmpty()) {
      log.info("An outbox message with saga id: {} is already processed!", restaurantApprovalResponse.getSagaId());
      return;
    }

    OrderApprovalOutboxMessage orderApprovalOutboxMessage = outboxMessageOptional.get();
    Order order = approveOrder(restaurantApprovalResponse);
    SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
    approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage,
        order.getOrderStatus(), sagaStatus));
    paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(restaurantApprovalResponse.getSagaId(),
        order.getOrderStatus(), sagaStatus));
    orderSagaHelper.saveOrder(order); log.info("Order with id: {} is approved", order.getId());
  }

  private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(String sagaId, OrderStatus orderStatus, SagaStatus sagaStatus) {
    Optional<OrderPaymentOutboxMessage> outboxMessageOptional = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
        UUID.fromString(sagaId),
        SagaStatus.PROCESSING);

    if (outboxMessageOptional.isEmpty()) {
      throw new OrderDomainException("Payment outbox message cannot be found in " +
          SagaStatus.PROCESSING.name() + " state");
    }

    OrderPaymentOutboxMessage orderPaymentOutboxMessage = outboxMessageOptional.get();
    orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
    orderPaymentOutboxMessage.setOrderStatus(orderStatus);
    orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
    return orderPaymentOutboxMessage;

  }

  private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(OrderApprovalOutboxMessage orderApprovalOutboxMessage,
      OrderStatus orderStatus, SagaStatus sagaStatus) {
    orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
    orderApprovalOutboxMessage.setOrderStatus(orderStatus);
    orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
    return orderApprovalOutboxMessage;
  }

  private Order approveOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
    log.info("Approving order with id: {}", restaurantApprovalResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
    orderDomainService.approveOrder(order);
    return order;
  }

  @Override
  @Transactional
  public void rollback(RestaurantApprovalResponse restaurantApprovalResponse) {

    Optional<OrderApprovalOutboxMessage> outboxMessageOptional = approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
        UUID.fromString(restaurantApprovalResponse.getSagaId()), SagaStatus.PROCESSING);

    if (outboxMessageOptional.isEmpty()) {
      log.info("An outbox message with saga id: {} is already roll backed!",
          restaurantApprovalResponse.getSagaId());
      return;
    }

    OrderApprovalOutboxMessage orderApprovalOutboxMessage = outboxMessageOptional.get();
    OrderCancelledEvent orderCancelledEvent = rollbackOrder(restaurantApprovalResponse);

    SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(
        orderCancelledEvent.getOrder().getOrderStatus());

    approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(orderApprovalOutboxMessage,
        orderCancelledEvent.getOrder().getOrderStatus(), sagaStatus));

    paymentOutboxHelper.savePaymentOutboxMessage(orderDataMapper
        .orderCancelledEventToOrderPaymentEventPayload(orderCancelledEvent),
        orderCancelledEvent.getOrder().getOrderStatus(),
        sagaStatus,
        OutboxStatus.STARTED,
        UUID.fromString(restaurantApprovalResponse.getSagaId()));

    log.info("Order with id: {} is cancelling", restaurantApprovalResponse.getOrderId());
  }

  private OrderCancelledEvent rollbackOrder(RestaurantApprovalResponse restaurantApprovalResponse) {
    log.info("Cancelling order with id: {}", restaurantApprovalResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(restaurantApprovalResponse.getOrderId());
    OrderCancelledEvent orderCancelledEvent = orderDomainService.cancelOrderPayment(order,
        restaurantApprovalResponse.getFailureMessages());
    orderSagaHelper.saveOrder(order);
    return orderCancelledEvent;
  }
}
