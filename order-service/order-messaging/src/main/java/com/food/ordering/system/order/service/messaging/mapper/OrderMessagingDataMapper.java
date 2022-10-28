package com.food.ordering.system.order.service.messaging.mapper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.create.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.create.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;

@Component
public class OrderMessagingDataMapper {

  public PaymentRequestAvroModel orderCreatedEventToPaymentRequestAvroModel(
      OrderCreatedEvent orderCreatedEvent) {
    Order order = orderCreatedEvent.getOrder();
    return PaymentRequestAvroModel.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSagaId("")
        .setCustomerId(order.getCustomerId().getValue().toString())
        .setOrderId(order.getId().getValue().toString())
        .setPrice(order.getPrice().getAmount())
        .setCreatedAt(orderCreatedEvent.getCreatedAt().toInstant())
        .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
        .build();
  }

  public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(
      OrderCancelledEvent orderCancelledEvent) {
    Order order = orderCancelledEvent.getOrder();
    return PaymentRequestAvroModel.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSagaId("")
        .setCustomerId(order.getCustomerId().getValue().toString())
        .setOrderId(order.getId().getValue().toString())
        .setPrice(order.getPrice().getAmount())
        .setCreatedAt(orderCancelledEvent.getCreatedAt().toInstant())
        .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
        .build();
  }

  public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantApprovalRequestAvroModel(OrderPaidEvent orderPaidEvent) {

    Order order = orderPaidEvent.getOrder();
    return RestaurantApprovalRequestAvroModel.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSagaId("")
        .setOrderId(order.getId().getValue().toString())
        .setRestaurantId(order.getRestaurantId().getValue().toString())
        .setOrderId(order.getId().getValue().toString())
        .setRestaurantOrderStatus(com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus
            .valueOf(order.getOrderStatus().name()))
        .setProducts(order.getItems().stream().map(orderItem ->
            com.food.ordering.system.kafka.order.avro.model.Product.newBuilder()
                .setId(orderItem.getProduct().getId().getValue().toString())
                .setQuantity(orderItem.getQuantity())
                .build())
            .collect(Collectors.toList()))
        .setPrice(order.getPrice().getAmount())
        .setCreatedAt(orderPaidEvent.getCreatedAt().toInstant())
        .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
        .build();
  }

  public PaymentResponse paymentResponseAvroModelToPaymentResponse(PaymentResponseAvroModel paymentResponseAvroModel) {
    return PaymentResponse.builder()
        .id(paymentResponseAvroModel.getId())
        .sagaId(paymentResponseAvroModel.getSagaId())
        .paymentId(paymentResponseAvroModel.getPaymentId())
        .customerId(paymentResponseAvroModel.getCustomerId())
        .orderId(paymentResponseAvroModel.getOrderId())
        .price(paymentResponseAvroModel.getPrice())
        .createdAt(paymentResponseAvroModel.getCreatedAt())
        .paymentStatus(com.food.ordering.system.domain.valueobject.PaymentStatus
            .valueOf(paymentResponseAvroModel.getPaymentOrderStatus().name()))
        .failureMessages(paymentResponseAvroModel.getFailureMessages())
        .build();
  }

  public RestaurantApprovalResponse approvalResponseAvroModelToApprovalResponse(
      RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
    return RestaurantApprovalResponse.builder()
        .id(restaurantApprovalResponseAvroModel.getId())
        .sagaId(restaurantApprovalResponseAvroModel.getSagaId())
        .restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
        .orderId(restaurantApprovalResponseAvroModel.getRestaurantId())
        .createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
        .orderApprovalStatus(com.food.ordering.system.domain.valueobject.OrderApprovalStatus.valueOf(
            restaurantApprovalResponseAvroModel.getOrderApprovalStatus().name()
        ))
        .failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
        .build();
  }
}
