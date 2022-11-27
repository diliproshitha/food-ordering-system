package com.food.ordering.system.order.service.domain;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.food.ordering.system.order.service.domain.dto.message.CustomerModel;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.inputs.message.listener.customer.CustomerMessageListener;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerMessageListenerImpl implements CustomerMessageListener {

  private final CustomerRepository customerRepository;
  private final OrderDataMapper orderDataMapper;

  @Override
  public void customerCreated(CustomerModel customerModel) {
    Customer customer = customerRepository.save(orderDataMapper.customerModelToCustomer(customerModel));

    if (Objects.isNull(customer)) {
      log.error("Customer could not be created in order database with id: {}", customerModel.getId());
      throw new OrderDomainException("Customer could not be created in order database with id: " +
          customerModel.getId());
    }
    log.info("Customer is created in order database with id: {}", customer.getId());
  }
}
