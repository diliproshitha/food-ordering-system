package com.food.ordering.system.restaurant.service.dataaccess.restaurant.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.respository.RestaurantJpaRepository;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {

  private final RestaurantJpaRepository restaurantJpaRepository;
  private final RestaurantDataAccessMapper restaurantDataAccessMapper;

  @Override
  public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
    List<UUID> restaurantProducts = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
    Optional<List<RestaurantEntity>> restaurantEntities = restaurantJpaRepository
        .findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), restaurantProducts);
    return restaurantEntities.map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
  }
}
