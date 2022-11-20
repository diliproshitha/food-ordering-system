package com.food.ordering.system.payment.service.domain;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;

import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.dataaccess.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.payment.service.dataaccess.outbox.repository.OrderOutboxJpaRepository;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.port.input.message.listener.PaymentRequestMessageListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = PaymentServiceApplication.class)
public class PaymentRequestMessageListenerTest {

  @Autowired
  private PaymentRequestMessageListener paymentRequestMessageListener;

  @Autowired
  private OrderOutboxJpaRepository orderOutboxJpaRepository;

  private static final String CUSTOMER_ID = "c4ac66fc-4f28-488c-86ec-7d54664e2fcb";
  private static final BigDecimal PRICE = new BigDecimal("100");

  @Test
  void testDoublePayment() {
    String sagaId = UUID.randomUUID().toString();
    paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
    try {
      paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
    } catch (DataAccessException e) {
      log.error("DataAccessException occurred with sql state: {}",
          ((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState());
    }
    assertOrderOutbox(sagaId);
  }

  @Test
  void testDoublePaymentWithThreads() {
    String sagaId = UUID.randomUUID().toString();
    ExecutorService executorService = null;

    executorService = Executors.newFixedThreadPool(2);
    List<Callable<Object>> tasks = new ArrayList<>();

    tasks.add(Executors.callable(() -> {
      try {
        paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
      } catch (DataAccessException e) {
        log.error("DataAccessException occurred for thread 1 with sql state: {}",
            ((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState());
      }
    }));

    tasks.add(Executors.callable(() -> {
      try {
        paymentRequestMessageListener.completePayment(getPaymentRequest(sagaId));
      } catch (DataAccessException e) {
        log.error("DataAccessException occurred for thread 2 with sql state: {}",
            ((PSQLException) Objects.requireNonNull(e.getRootCause())).getSQLState());
      }
    }));

    try {
      executorService.invokeAll(tasks);
      assertOrderOutbox(sagaId);
    } catch (InterruptedException e) {
      log.error("Error calling complete payment!", e);
    } finally {
      if (executorService != null) {
        executorService.shutdown();
      }
    }
  }

  private void assertOrderOutbox(String sagaId) {
    Optional<OrderOutboxEntity> orderOutboxEntity = orderOutboxJpaRepository.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(
        ORDER_SAGA_NAME,
        UUID.fromString(sagaId),
        PaymentStatus.COMPLETED,
        OutboxStatus.STARTED);

    assertTrue(orderOutboxEntity.isPresent());
    assertEquals(orderOutboxEntity.get().getSagaId().toString(), sagaId);
  }

  private PaymentRequest getPaymentRequest(String sagaId) {
    return PaymentRequest.builder()
        .id(UUID.randomUUID().toString())
        .sagaId(sagaId)
        .orderId(UUID.randomUUID().toString())
        .paymentOrderStatus(PaymentOrderStatus.PENDING)
        .customerId(CUSTOMER_ID)
        .price(PRICE)
        .createdAt(Instant.now())
        .build();
  }
}
