package com.food.ordering.system.restaurant.service.dataaccess.restaurant.adapter;

import org.springframework.stereotype.Component;

import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.repository.OrderApprovalJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderApprovalRepositoryImpl implements OrderApprovalRepository {

  private final OrderApprovalJpaRepository orderApprovalJpaRepository;
  private final RestaurantDataAccessMapper restaurantDataAccessMapper;

  @Override
  public OrderApproval save(OrderApproval orderApproval) {
    return restaurantDataAccessMapper.orderApprovalEntityToOrderApproval(orderApprovalJpaRepository
        .save(restaurantDataAccessMapper.orderApprovalToOrderApprovalEntity(orderApproval)));
  }
}
