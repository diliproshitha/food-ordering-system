package com.food.ordering.system.dataaccess.restaurant.respository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntityId;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, RestaurantEntityId> {

  Optional<List<RestaurantEntity>> findByRestaurantIdAndProductIdIn(UUID restaurantId,
      List<UUID> productIds);
}
