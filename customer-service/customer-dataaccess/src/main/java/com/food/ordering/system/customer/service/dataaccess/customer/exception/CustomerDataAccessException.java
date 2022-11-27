package com.food.ordering.system.customer.service.dataaccess.customer.exception;

import com.food.ordering.system.domain.exception.DomainException;

public class CustomerDataAccessException extends DomainException {

  public CustomerDataAccessException(String message) {
    super(message);
  }
}
