package com.food.ordering.system.customer.service.domain;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomerDomainServiceImpl implements CustomerDomainService {

  @Override
  public CustomerCreatedEvent validateAndInitiateCustomer(Customer customer) {
    // Any business logic requires to run for a customer creation
    log.info("Customer with id: {} is initiated", customer.getId().getValue());
    return new CustomerCreatedEvent(customer, ZonedDateTime.now(ZoneId.of(UTC)));
  }
}
