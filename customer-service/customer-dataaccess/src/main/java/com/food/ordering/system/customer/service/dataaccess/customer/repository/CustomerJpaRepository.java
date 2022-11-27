package com.food.ordering.system.customer.service.dataaccess.customer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.food.ordering.system.customer.service.dataaccess.customer.entity.CustomerEntity;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {

}
