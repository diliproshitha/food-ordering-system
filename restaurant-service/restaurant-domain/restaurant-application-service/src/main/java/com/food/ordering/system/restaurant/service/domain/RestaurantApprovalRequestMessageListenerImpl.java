package com.food.ordering.system.restaurant.service.domain;

import org.springframework.stereotype.Service;

import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalRequestMessageListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestaurantApprovalRequestMessageListenerImpl implements RestaurantApprovalRequestMessageListener {

  private final RestaurantApprovalRequestHelper restaurantApprovalRequestHelper;

  @Override
  public void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest) {
    restaurantApprovalRequestHelper.persistOrderApproval(restaurantApprovalRequest);
  }
}
