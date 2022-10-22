package com.food.ordering.system.order.service.domain.ports.outputs.repository;

import java.util.Optional;
import java.util.UUID;

import com.food.ordering.system.order.service.domain.entity.Customer;

public interface CustomerRepository {

  Optional<Customer> findCustomer(UUID customerId);

}
