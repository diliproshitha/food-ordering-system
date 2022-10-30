package com.food.ordering.system.payment.service.domain.entity;

import static com.food.ordering.system.domain.DomainConstants.UTC;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.valueobject.PaymentId;

public class Payment extends AggregateRoot<PaymentId> {

  private final OrderId orderId;
  private final CustomerId customerId;
  private final Money price;

  private PaymentStatus paymentStatus;
  private ZonedDateTime createdAt;

  private Payment(Builder builder) {
    super.setId(builder.id);
    orderId = builder.orderId;
    customerId = builder.customerId;
    price = builder.price;
    paymentStatus = builder.paymentStatus;
    createdAt = builder.createdAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void initializePayment() {
    setId(new PaymentId(UUID.randomUUID()));
    createdAt = ZonedDateTime.now(ZoneId.of(UTC));
  }

  public void validatePayment(List<String> failureMessages) {
    if (Objects.isNull(price) || !price.isGreaterThanZero()) {
      failureMessages.add("Total price must be greater than zero!");
    }
  }

  public void updateStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public OrderId getOrderId() {
    return orderId;
  }

  public CustomerId getCustomerId() {
    return customerId;
  }

  public Money getPrice() {
    return price;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }


  public static final class Builder {

    private PaymentId id;
    private OrderId orderId;
    private CustomerId customerId;
    private Money price;
    private PaymentStatus paymentStatus;
    private ZonedDateTime createdAt;

    private Builder() {
    }

    public Builder id(PaymentId val) {
      id = val;
      return this;
    }

    public Builder orderId(OrderId val) {
      orderId = val;
      return this;
    }

    public Builder customerId(CustomerId val) {
      customerId = val;
      return this;
    }

    public Builder price(Money val) {
      price = val;
      return this;
    }

    public Builder paymentStatus(PaymentStatus val) {
      paymentStatus = val;
      return this;
    }

    public Builder createdAt(ZonedDateTime val) {
      createdAt = val;
      return this;
    }

    public Payment build() {
      return new Payment(this);
    }
  }
}
