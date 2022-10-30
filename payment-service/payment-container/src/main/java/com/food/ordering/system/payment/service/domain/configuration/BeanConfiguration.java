package com.food.ordering.system.payment.service.domain.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.food.ordering.system.payment.service.domain.PaymentDomainService;
import com.food.ordering.system.payment.service.domain.PaymentDomainServiceImpl;

@Configuration
public class BeanConfiguration {

  @Bean
  public PaymentDomainService paymentDomainService() {
    return new PaymentDomainServiceImpl();
  }
}
