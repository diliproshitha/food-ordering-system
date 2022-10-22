package com.food.ordering.system.order.service.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.RestaurantRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderCreateCommandHandler {

  private final OrderDomainService orderDomainService;
  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final RestaurantRepository restaurantRepository;
  private final OrderDataMapper orderDataMapper;

  public OrderCreateCommandHandler(OrderDomainService orderDomainService,
      OrderRepository orderRepository, CustomerRepository customerRepository,
      RestaurantRepository restaurantRepository, OrderDataMapper orderDataMapper) {
    this.orderDomainService = orderDomainService;
    this.orderRepository = orderRepository;
    this.customerRepository = customerRepository;
    this.restaurantRepository = restaurantRepository;
    this.orderDataMapper = orderDataMapper;
  }

  @Transactional
  public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
    checkCustomer(createOrderCommand.getCustomerId());
    Restaurant restaurant = checkRestaurant(createOrderCommand);
    Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
    OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order,
        restaurant);
    Order orderResult = saveOrder(order);
    return orderDataMapper.orderToCreateOrderResponse(orderResult);
  }

  private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
    Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(
        createOrderCommand);
    Optional<Restaurant> restaurantInformation = restaurantRepository.findRestaurantInformation(
        restaurant);

    if (restaurantInformation.isEmpty()) {
      log.warn("Could not find a restaurant with id: {}", createOrderCommand.getRestaurantId());
      throw new OrderDomainException("Could not find a restaurant with id: " + createOrderCommand.getRestaurantId());
    }
    return restaurantInformation.get();
  }

  private void checkCustomer(UUID customerId) {
    Optional<Customer> customer = customerRepository.findCustomer(customerId);

    if (customer.isEmpty()) {
      log.warn("Could not find a customer with id: {}", customerId);
      throw new OrderDomainException("Could not find a customer with id: " + customerId);
    }
  }

  private Order saveOrder(Order order) {
    Order orderResult = orderRepository.save(order);
    if (Objects.isNull(orderResult)) {
      log.info("Could not save order!");
      throw new OrderDomainException("Could not save order!");
    }
    log.info("Order is saved with id: {}", orderResult.getId());
    return orderResult;
  }
}
