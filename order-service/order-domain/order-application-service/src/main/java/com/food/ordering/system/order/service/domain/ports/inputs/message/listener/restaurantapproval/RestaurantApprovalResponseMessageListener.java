package com.food.ordering.system.order.service.domain.ports.inputs.message.listener.restaurantapproval;

import com.food.ordering.system.order.service.domain.dto.create.message.RestaurantApprovalResponse;

public interface RestaurantApprovalResponseMessageListener {

  void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse);

  void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);
}
