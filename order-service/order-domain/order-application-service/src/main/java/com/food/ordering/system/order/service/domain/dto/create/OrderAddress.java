package com.food.ordering.system.order.service.domain.dto.create;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class OrderAddress {

  @NotNull
  @Max(value = 50)
  private final String street;
  @NotNull
  @Max(value = 10)
  private final String postalCode;
  @NotNull
  @Max(value = 50)
  private final String city;
}
