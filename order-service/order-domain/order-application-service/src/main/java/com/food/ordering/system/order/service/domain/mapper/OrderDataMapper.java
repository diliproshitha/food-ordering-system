package com.food.ordering.system.order.service.domain.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.domain.valueobject.RestaurantOrderStatus;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventProduct;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;

@Component
public class OrderDataMapper {

  public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
    return Restaurant.builder()
        .id(new RestaurantId(createOrderCommand.getRestaurantId()))
        .products(createOrderCommand.getItems().stream().map(orderItem ->
                new Product(new ProductId(orderItem.getProductId())))
            .collect(Collectors.toList()))
        .build();
  }

  public OrderApprovalEventPayload orderPaidEventToOrderApprovalEventPayload(OrderPaidEvent orderPaidEvent) {
    return OrderApprovalEventPayload.builder()
        .orderId(orderPaidEvent.getOrder().getId().getValue().toString())
        .restaurantId(orderPaidEvent.getOrder().getRestaurantId().getValue().toString())
        .restaurantOrderStatus(RestaurantOrderStatus.PAID.name())
        .products(orderPaidEvent.getOrder().getItems().stream().map(orderItem ->
            OrderApprovalEventProduct.builder()
                .id(orderItem.getProduct().getId().getValue().toString())
                .quantity(orderItem.getQuantity())
                .build()).collect(Collectors.toList()))
        .price(orderPaidEvent.getOrder().getPrice().getAmount())
        .createdAt(orderPaidEvent.getCreatedAt())
        .build();
  }

  public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
    return Order.builder()
        .customerId(new CustomerId(createOrderCommand.getCustomerId()))
        .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
        .deliverAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
        .price(new Money(createOrderCommand.getPrice()))
        .items(orderItemsToOrderItemEntities(createOrderCommand.getItems()))
        .build();
  }

  private List<OrderItem> orderItemsToOrderItemEntities(
      List<com.food.ordering.system.order.service.domain.dto.create.OrderItem> items) {
    return items.stream()
        .map(orderItem ->
            OrderItem.builder()
                .product(new Product(new ProductId(orderItem.getProductId())))
                .price(new Money(orderItem.getPrice()))
                .quantity(orderItem.getQuantity())
                .subTotal(new Money(orderItem.getSubTotal()))
                .build())
        .collect(Collectors.toList());
  }

  private StreetAddress orderAddressToStreetAddress(OrderAddress address) {
    return new StreetAddress(
        UUID.randomUUID(),
        address.getStreet(),
        address.getPostalCode(),
        address.getCity()
    );
  }

  public CreateOrderResponse orderToCreateOrderResponse(Order order, String message) {
    return CreateOrderResponse.builder()
        .orderTrackingId(order.getTrackingId().getValue())
        .orderStatus(order.getOrderStatus())
        .message(message)
        .build();
  }

  public TrackOrderResponse orderToTrackOrderResponse(Order order) {
    return TrackOrderResponse.builder()
        .orderTrackingId(order.getTrackingId().getValue())
        .orderStatus(order.getOrderStatus())
        .failureMessages(order.getFailureMessages())
        .build();
  }

  public OrderPaymentEventPayload orderCreatedEventToOrderPaymentEventPayload(
      OrderCreatedEvent orderCreatedEvent) {
    return OrderPaymentEventPayload.builder()
        .customerId(orderCreatedEvent.getOrder().getCustomerId().getValue().toString())
        .orderId(orderCreatedEvent.getOrder().getId().getValue().toString())
        .price(orderCreatedEvent.getOrder().getPrice().getAmount())
        .createdAt(orderCreatedEvent.getCreatedAt())
        .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
        .build();
  }

  public OrderPaymentEventPayload orderCancelledEventToOrderPaymentEventPayload(OrderCancelledEvent orderCancelledEvent) {
    return OrderPaymentEventPayload.builder()
        .customerId(orderCancelledEvent.getOrder().getCustomerId().getValue().toString())
        .orderId(orderCancelledEvent.getOrder().getId().getValue().toString())
        .price(orderCancelledEvent.getOrder().getPrice().getAmount())
        .createdAt(orderCancelledEvent.getCreatedAt())
        .paymentOrderStatus(PaymentOrderStatus.CANCELLED.name())
        .build();
  }
}
