package com.food.ordering.system.order.service.domain.dto.track;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class TrackOrderQuery {

  @NotNull
  private final UUID orderTrackingId;

}
