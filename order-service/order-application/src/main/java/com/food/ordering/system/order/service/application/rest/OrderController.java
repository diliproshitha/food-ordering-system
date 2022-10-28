package com.food.ordering.system.order.service.application.rest;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.ports.inputs.service.OrderApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/orders", produces = "application/vnd.api.v1+json")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

  private final OrderApplicationService orderApplicationService;

  @PostMapping
  public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderCommand createOrderCommand) {
    log.info("Creating order for customer: {} at restaurant: {}", createOrderCommand.getCustomerId(),
        createOrderCommand.getRestaurantId());
    CreateOrderResponse orderResponse = orderApplicationService.createOrder(createOrderCommand);
    log.info("Order created with tracking id: {}", orderResponse.getOrderTrackingId());
    return ResponseEntity.ok(orderResponse);
  }

  @GetMapping("/{trackingId}")
  public ResponseEntity<TrackOrderResponse> getByTrackingId(@PathVariable UUID trackingId) {
    TrackOrderResponse trackOrderResponse = orderApplicationService.trackOrder(
        TrackOrderQuery.builder().orderTrackingId(trackingId).build());
    log.info("Returning order status with tracking id: {}", trackOrderResponse.getOrderTrackingId());
    return ResponseEntity.ok(trackOrderResponse);
  }

}
