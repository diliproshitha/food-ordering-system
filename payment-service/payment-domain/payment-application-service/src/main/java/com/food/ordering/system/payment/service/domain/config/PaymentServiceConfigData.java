package com.food.ordering.system.payment.service.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "payment-service")
public class PaymentServiceConfigData {

  private String paymentRequest;
  private String paymentResponseTopicName;


}
