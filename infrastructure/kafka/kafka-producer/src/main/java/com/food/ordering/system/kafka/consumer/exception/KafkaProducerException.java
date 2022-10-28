package com.food.ordering.system.kafka.consumer.exception;

public class KafkaProducerException extends RuntimeException{

  public KafkaProducerException(String message) {
    super(message);
  }
}
