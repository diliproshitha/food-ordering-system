package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.domain.dto.create.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.OrderRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {

  private final OrderDomainService orderDomainService;
  private final OrderSagaHelper orderSagaHelper;
  private final PaymentOutboxHelper paymentOutboxHelper;
  private final OrderRepository orderRepository;
  private final ApprovalOutboxHelper approvalOutboxHelper;
  private final OrderDataMapper orderDataMapper;

  @Override
  @Transactional
  public void process(PaymentResponse paymentResponse) {

    Optional<OrderPaymentOutboxMessage> paymentMessagesOptional = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
        UUID.fromString(paymentResponse.getSagaId()),
        SagaStatus.STARTED);

    if (paymentMessagesOptional.isEmpty()) {
      log.info("An outbox message with saga id: {} is already processed!", paymentResponse.getSagaId());
      return;
    }

    OrderPaymentOutboxMessage orderPaymentOutboxMessage = paymentMessagesOptional.get();
    OrderPaidEvent orderPaidEvent = completePaymentForOrder(paymentResponse);

    SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(
        orderPaidEvent.getOrder().getOrderStatus());
    paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage, 
        orderPaidEvent.getOrder().getOrderStatus(), sagaStatus));

    approvalOutboxHelper.saveApprovalOutboxMessage(orderDataMapper.orderPaidEventToOrderApprovalEventPayload(orderPaidEvent),
        orderPaidEvent.getOrder().getOrderStatus(), sagaStatus, OutboxStatus.STARTED,
        UUID.fromString(paymentResponse.getSagaId()));

    log.info("Order with id: {} is paid", orderPaidEvent.getOrder()
        .getId().getValue());
  }

  private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
      OrderStatus orderStatus, SagaStatus sagaStatus) {
    orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
    orderPaymentOutboxMessage.setOrderStatus(orderStatus);
    orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
    return  orderPaymentOutboxMessage;
  }

  @Override
  @Transactional
  public void rollback(PaymentResponse paymentResponse) {

    Optional<OrderPaymentOutboxMessage> outboxMessageOptional = paymentOutboxHelper
        .getPaymentOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(paymentResponse.getSagaId()),
        getCurrentSagaStatus(paymentResponse.getPaymentStatus()));

    if (outboxMessageOptional.isEmpty()) {
      log.info("An outbox message with saga id: {} is already roll backed!", paymentResponse.getSagaId());
      return;
    }

    OrderPaymentOutboxMessage orderPaymentOutboxMessage = outboxMessageOptional.get();
    Order order = rollBackPaymentForOrder(paymentResponse);
    SagaStatus sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
    paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage,
        order.getOrderStatus(), sagaStatus));

    if (paymentResponse.getPaymentStatus() == PaymentStatus.CANCELLED) {
      approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(paymentResponse.getSagaId(),
          order.getOrderStatus(), sagaStatus));
    }

    log.info("Order with id: {} is cancelled", order.getId().getValue());
  }

  private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(String sagaId,
      OrderStatus orderStatus, SagaStatus sagaStatus) {
    Optional<OrderApprovalOutboxMessage> approvalMessageOptional = approvalOutboxHelper
        .getApprovalOutboxMessageBySagaIdAndSagaStatus(UUID.fromString(sagaId), SagaStatus.COMPENSATING);

    if (approvalMessageOptional.isEmpty()) {
      throw new OrderDomainException("Approval outbox message could not be found in " +
          SagaStatus.COMPENSATING.name() + " status!");
    }

    OrderApprovalOutboxMessage orderApprovalOutboxMessage = approvalMessageOptional.get();
    orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
    orderApprovalOutboxMessage.setOrderStatus(orderStatus);
    orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
    return orderApprovalOutboxMessage;
  }

  private Order rollBackPaymentForOrder(PaymentResponse paymentResponse) {
    log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
    orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
    orderSagaHelper.saveOrder(order);
    return order;
  }

  private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
    return switch (paymentStatus) {
      case COMPLETED -> new SagaStatus[] {SagaStatus.STARTED};
      case CANCELLED -> new SagaStatus[] {SagaStatus.PROCESSING};
      case FAILED -> new SagaStatus[] {SagaStatus.STARTED, SagaStatus.PROCESSING};
    };
  }

  private OrderPaidEvent completePaymentForOrder(PaymentResponse paymentResponse) {
    log.info("Completing payment for order with id: {}", paymentResponse.getOrderId());
    Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
    OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);
    orderRepository.save(order);
    return orderPaidEvent;
  }
}
