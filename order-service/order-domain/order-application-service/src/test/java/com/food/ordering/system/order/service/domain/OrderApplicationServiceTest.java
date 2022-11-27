package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.inputs.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.PaymentOutboxRepository;
import com.food.ordering.system.order.service.domain.ports.outputs.repository.RestaurantRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceTest {

  @Autowired
  private OrderApplicationService orderApplicationService;

  @Autowired
  private OrderDataMapper orderDataMapper;

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private PaymentOutboxRepository paymentOutboxRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private CreateOrderCommand createOrderCommand;
  private CreateOrderCommand createOrderCommandWrongPrice;
  private CreateOrderCommand createOrderCommandWrongProductPrice;

  private final UUID CUSTOMER_ID = UUID.fromString("f4d54d55-f883-46fc-b8a6-7a10aed6c08e");
  private final UUID RESTAURANT_ID = UUID.fromString("acd663ec-d359-4a82-a7d9-17f513f252d5");
  private final UUID PRODUCT_ID = UUID.fromString("e310f3f0-9062-495e-8064-094acbf72431");
  private final UUID ORDER_ID = UUID.fromString("1a2794dc-ec1b-4eff-90d4-1dc590b80a1c");
  private final UUID SAGA_ID = UUID.fromString("e54515b6-20a5-479a-b946-dc9a7130a374");
  private final BigDecimal PRICE = new BigDecimal("200.00");

  @BeforeAll
  private void init() {
    createOrderCommand = CreateOrderCommand.builder()
        .customerId(CUSTOMER_ID)
        .restaurantId(RESTAURANT_ID)
        .address(OrderAddress.builder()
            .street("street_1")
            .postalCode("1000AB")
            .city("Paris")
            .build())
        .price(PRICE)
        .items(List.of(OrderItem.builder()
            .productId(PRODUCT_ID)
            .quantity(1)
            .price(new BigDecimal("50.00"))
            .subTotal(new BigDecimal("50.00"))
            .build(),
            OrderItem.builder()
                .productId(PRODUCT_ID)
                .quantity(3)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("150.00"))
                .build()
        ))
        .build();

    createOrderCommandWrongPrice = CreateOrderCommand.builder()
        .customerId(CUSTOMER_ID)
        .restaurantId(RESTAURANT_ID)
        .address(OrderAddress.builder()
            .street("street_1")
            .postalCode("1000AB")
            .city("Paris")
            .build())
        .price(new BigDecimal("250.00"))
        .items(List.of(OrderItem.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("50.00"))
                .build(),
            OrderItem.builder()
                .productId(PRODUCT_ID)
                .quantity(3)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("150.00"))
                .build()
        ))
        .build();

    createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
        .customerId(CUSTOMER_ID)
        .restaurantId(RESTAURANT_ID)
        .address(OrderAddress.builder()
            .street("street_1")
            .postalCode("1000AB")
            .city("Paris")
            .build())
        .price(new BigDecimal("210.00"))
        .items(List.of(OrderItem.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .price(new BigDecimal("60.00"))
                .subTotal(new BigDecimal("60.00"))
                .build(),
            OrderItem.builder()
                .productId(PRODUCT_ID)
                .quantity(3)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("150.00"))
                .build()
        ))
        .build();

    Customer customer = new Customer(new CustomerId(CUSTOMER_ID));
    customer.setId(new CustomerId(CUSTOMER_ID));

    Restaurant restaurantResponse = Restaurant.builder()
        .id(new RestaurantId(createOrderCommand.getRestaurantId()))
        .products(List.of(new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
            new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))))
        .active(true)
        .build();

    Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
    order.setId(new OrderId(ORDER_ID));

    when(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
        .thenReturn(Optional.of(restaurantResponse));

    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(paymentOutboxRepository.save(any(OrderPaymentOutboxMessage.class))).thenReturn(getOrderPaymentOutboxMessage());
  }

  private OrderPaymentOutboxMessage getOrderPaymentOutboxMessage() {
    OrderPaymentEventPayload orderPaymentEventPayload = OrderPaymentEventPayload.builder()
        .orderId(ORDER_ID.toString())
        .customerId(CUSTOMER_ID.toString())
        .price(PRICE)
        .createdAt(ZonedDateTime.now())
        .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
        .build();

    return OrderPaymentOutboxMessage.builder()
        .id(UUID.randomUUID())
        .sagaId(SAGA_ID)
        .createdAt(ZonedDateTime.now())
        .type(ORDER_SAGA_NAME)
        .payload(createPayload(orderPaymentEventPayload))
        .orderStatus(OrderStatus.PENDING)
        .sagaStatus(SagaStatus.STARTED)
        .outboxStatus(OutboxStatus.STARTED)
        .version(0)
        .build();
  }

  private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
    try {
      return objectMapper.writeValueAsString(orderPaymentEventPayload);
    } catch (JsonProcessingException e) {
      throw new OrderDomainException("Cannot create OrderPaymentEventPayload object!", e);
    }
  }

  @Test
  public void testCreateOrder() {
    CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
    assertEquals(OrderStatus.PENDING, createOrderResponse.getOrderStatus());
    assertEquals("Order created successfully!", createOrderResponse.getMessage());
    assertNotNull(createOrderResponse.getOrderTrackingId());
  }

  @Test
  public void testCreateOrderWithWrongTotalPrice() {
    OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
        () -> orderApplicationService.createOrder(createOrderCommandWrongPrice));
    assertEquals("Total price: " + createOrderCommandWrongPrice.getPrice() +
            " is not equals to Order items total: 200.00!", orderDomainException.getMessage());
  }

  @Test
  public void testCreateOrderWithWrongProductPrice() {
    OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
        () -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));
    assertEquals("Order item price: 60.00 is not valid for product " + PRODUCT_ID,
        orderDomainException.getMessage());
  }

  @Test
  public void testCreateOrderWithPassiveRestaurant() {
    Restaurant passiveRestaurantResponse = Restaurant.builder()
        .id(new RestaurantId(createOrderCommand.getRestaurantId()))
        .products(List.of(new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
            new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))))
        .active(false)
        .build();

    when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
        .thenReturn(Optional.of(passiveRestaurantResponse));
    OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
        () -> orderApplicationService.createOrder(createOrderCommand));

    assertEquals("Restaurant with id " + createOrderCommand.getRestaurantId() + " is currently inactive",
        orderDomainException.getMessage());
  }


}
