package com.food.ordering.system.order.service.domain.dto.create.message;

import java.time.Instant;
import java.util.List;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;

import lombok.Builder;

@Builder
public class RestaurantApprovalResponse {

  private String id;
  private String sagaId;
  private String orderId;
  private String restaurantId;
  private String customerId;
  private Instant createdAt;
  private OrderApprovalStatus orderApprovalStatus;
  private List<String> failureMessages;

}
