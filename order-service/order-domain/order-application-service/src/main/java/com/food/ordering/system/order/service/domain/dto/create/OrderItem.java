package com.food.ordering.system.order.service.domain.dto.create;

import java.math.BigDecimal;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class OrderItem {

  @NotNull
  private final UUID productId;
  @NotNull
  private final Integer quantity;
  @NotNull
  private final BigDecimal price;
  @NotNull
  private final BigDecimal subTotal;
}
