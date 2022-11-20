package com.food.ordering.system.restaurant.service.domain.mapper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;

@Component
public class RestaurantDataMapper {

  public Restaurant restaurantApprovalRequestAvroModelToRestaurant(
      RestaurantApprovalRequest restaurantApprovalRequest) {
    return Restaurant.builder()
        .id(new RestaurantId(UUID.fromString(restaurantApprovalRequest.getRestaurantId())))
        .orderDetail(OrderDetail.builder()
            .id(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())))
            .products(restaurantApprovalRequest.getProducts().stream().map(
                    product -> Product.builder()
                        .id(product.getId())
                        .quantity(product.getQuantity())
                        .build())
                .collect(Collectors.toList()))
            .totalAmount(new Money(restaurantApprovalRequest.getPrice()))
            .orderStatus(OrderStatus.valueOf(restaurantApprovalRequest.getRestaurantOrderStatus().name()))
            .build())
        .build();
  }

  public OrderEventPayload orderApprovalEventToOrderEventPayload(OrderApprovalEvent orderApprovalEvent) {
    return OrderEventPayload.builder()
        .orderId(orderApprovalEvent.getOrderApproval().getOrderId().getValue().toString())
        .restaurantId(orderApprovalEvent.getRestaurantId().getValue().toString())
        .orderApprovalStatus(orderApprovalEvent.getOrderApproval().getApprovalStatus().name())
        .createdAt(orderApprovalEvent.getCreatedAt())
        .failureMessages(orderApprovalEvent.getFailureMessages())
        .build();
  }
}
