package com.food.ordering.system.customer.service.messaging.mapper;

import org.springframework.stereotype.Component;

import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;

@Component
public class CustomerMessagingDataMapper {

  public CustomerAvroModel paymentResponseAvroModelToPaymentResponse(CustomerCreatedEvent customerCreatedEvent) {
    return CustomerAvroModel.newBuilder()
        .setId(customerCreatedEvent.getCustomer().getId().getValue().toString())
        .setUsername(customerCreatedEvent.getCustomer().getUsername())
        .setFirstName(customerCreatedEvent.getCustomer().getFirstName())
        .setLastName(customerCreatedEvent.getCustomer().getLastName())
        .build();
  }

}
