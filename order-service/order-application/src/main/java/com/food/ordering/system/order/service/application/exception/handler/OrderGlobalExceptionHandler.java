package com.food.ordering.system.order.service.application.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.food.ordering.system.application.handler.ErrorDto;
import com.food.ordering.system.application.handler.GlobalExceptionHandler;
import com.food.ordering.system.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class OrderGlobalExceptionHandler extends GlobalExceptionHandler {

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(value = {OrderDomainException.class})
  public ErrorDto handleException(OrderDomainException orderDomainException) {
    log.error(orderDomainException.getMessage(), orderDomainException);
    return ErrorDto.builder()
        .message(orderDomainException.getMessage())
        .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .build();
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(value = {OrderNotFoundException.class})
  public ErrorDto handleException(OrderNotFoundException orderDomainException) {
    log.error(orderDomainException.getMessage(), orderDomainException);
    return ErrorDto.builder()
        .message(orderDomainException.getMessage())
        .code(HttpStatus.NOT_FOUND.getReasonPhrase())
        .build();
  }

}
