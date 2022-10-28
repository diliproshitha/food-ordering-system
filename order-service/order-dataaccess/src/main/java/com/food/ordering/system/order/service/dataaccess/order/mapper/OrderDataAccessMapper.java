package com.food.ordering.system.order.service.dataaccess.order.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

@Component
public class OrderDataAccessMapper {

  public OrderEntity orderToOrderEntity(Order order) {
    OrderEntity orderEntity = OrderEntity.builder()
        .id(order.getId().getValue())
        .customerId(order.getCustomerId().getValue())
        .restaurantId(order.getRestaurantId().getValue())
        .trackingId(order.getTrackingId().getValue())
        .address(deliverAddressToAddressEntity(order.getDeliverAddress()))
        .price(order.getPrice().getAmount())
        .items(orderItemsToOrderItemEntities(order.getItems()))
        .orderStatus(order.getOrderStatus())
        .failureMessages(!CollectionUtils.isEmpty(order.getFailureMessages()) ?
            String.join(Order.FAILURE_MESSAGE_DELIMITER, order.getFailureMessages()) : "")
        .build();
    orderEntity.getAddress().setOrder(orderEntity);
    orderEntity.getItems().forEach(orderItemEntity -> orderItemEntity.setOrder(orderEntity));

    return orderEntity;
  }

  public Order orderEntityToOrder(OrderEntity orderEntity) {
    return Order.builder()
        .id(new OrderId(orderEntity.getId()))
        .customerId(new CustomerId(orderEntity.getCustomerId()))
        .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
        .deliverAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
        .price(new Money(orderEntity.getPrice()))
        .items(orderItemEntitiesToOrderItems(orderEntity.getItems()))
        .trackingId(new TrackingId(orderEntity.getTrackingId()))
        .orderStatus(orderEntity.getOrderStatus())
        .failureMessages(orderEntity.getFailureMessages().isEmpty() ? new ArrayList<>() :
            new ArrayList<>(Arrays.asList(orderEntity.getFailureMessages()
                .split(Order.FAILURE_MESSAGE_DELIMITER))))
        .build();
  }

  private List<OrderItem> orderItemEntitiesToOrderItems(List<OrderItemEntity> items) {
    return items.stream()
        .map(orderItemEntity -> OrderItem.builder()
            .id(new OrderItemId(orderItemEntity.getId()))
            .product(new Product(new ProductId(orderItemEntity.getProductId())))
            .price(new Money(orderItemEntity.getPrice()))
            .quantity(orderItemEntity.getQuantity())
            .subTotal(new Money(orderItemEntity.getSubTotal()))
            .build())
        .collect(Collectors.toList());
  }

  private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity address) {
    return new StreetAddress(address.getId(), address.getStreet(), address.getPostalCode(), address.getCity());
  }

  private List<OrderItemEntity> orderItemsToOrderItemEntities(List<OrderItem> items) {
    return items.stream()
        .map(orderItem -> OrderItemEntity.builder()
            .id(orderItem.getId().getValue())
            .productId(orderItem.getProduct().getId().getValue())
            .price(orderItem.getPrice().getAmount())
            .quantity(orderItem.getQuantity())
            .subTotal(orderItem.getSubTotal().getAmount())
            .build())
        .collect(Collectors.toList());
  }

  private OrderAddressEntity deliverAddressToAddressEntity(StreetAddress deliverAddress) {
    return OrderAddressEntity.builder()
        .id(deliverAddress.getId())
        .street(deliverAddress.getSteet())
        .postalCode(deliverAddress.getPostalCode())
        .city(deliverAddress.getCity())
        .build();
  }
}
