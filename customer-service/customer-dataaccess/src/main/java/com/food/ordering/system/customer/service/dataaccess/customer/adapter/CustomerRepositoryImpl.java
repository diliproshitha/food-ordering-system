package com.food.ordering.system.customer.service.dataaccess.customer.adapter;

import org.springframework.stereotype.Component;

import com.food.ordering.system.customer.service.dataaccess.customer.mapper.CustomerDataAccessMapper;
import com.food.ordering.system.customer.service.dataaccess.customer.repository.CustomerJpaRepository;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

  private final CustomerJpaRepository customerJpaRepository;
  private final CustomerDataAccessMapper customerDataAccessMapper;

  @Override
  public Customer createCustomer(Customer customer) {
    return customerDataAccessMapper
        .customerEntityToCustomer(customerJpaRepository
            .save(customerDataAccessMapper.customerToCustomerEntity(customer)));
  }
}
