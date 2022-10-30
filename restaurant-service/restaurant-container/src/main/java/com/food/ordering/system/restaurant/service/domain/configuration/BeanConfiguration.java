package com.food.ordering.system.restaurant.service.domain.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.food.ordering.system.restaurant.service.domain.RestaurantDomainService;
import com.food.ordering.system.restaurant.service.domain.RestaurantDomainServiceImpl;

@Configuration
public class BeanConfiguration {

  @Bean
  public RestaurantDomainService restaurantDomainService() {
    return new RestaurantDomainServiceImpl();
  }
}
