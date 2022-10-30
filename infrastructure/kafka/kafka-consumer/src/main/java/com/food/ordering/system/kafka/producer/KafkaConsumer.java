package com.food.ordering.system.kafka.producer;

import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;

public interface KafkaConsumer <T extends SpecificRecordBase>{

  void receive(List<T> messages, List<String> keys, List<Integer> partitions, List<Long> offsets);

}
