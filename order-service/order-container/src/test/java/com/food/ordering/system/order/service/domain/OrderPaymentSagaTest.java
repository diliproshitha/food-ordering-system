package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import com.food.ordering.system.order.service.domain.dto.create.message.PaymentResponse;
import com.food.ordering.system.saga.SagaStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = OrderServiceApplication.class)
@Sql(value = {"classpath:sql/OrderPaymentSagaTestSetUp.sql"})
@Sql(value = {
    "classpath:sql/OrderPaymentSagaTestCleanUp.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class OrderPaymentSagaTest {

  @Autowired
  private OrderPaymentSaga orderPaymentSaga;

  @Autowired
  private PaymentOutboxJpaRepository paymentOutboxJpaRepository;

  private final UUID SAGA_ID = UUID.fromString("3d176b23-7723-43b0-b61e-47e9bd000938");
  private final UUID ORDER_ID = UUID.fromString("3148c572-540d-480d-9485-4906c7854e8a");
  private final UUID CUSTOMER_ID = UUID.fromString("c4ac66fc-4f28-488c-86ec-7d54664e2fcb");
  private final UUID PAYMENT_ID = UUID.randomUUID();
  private final BigDecimal PRICE = new BigDecimal("100");

  @Test
  void testDoublePayment() {
    orderPaymentSaga.process(getPaymentResponse());
    orderPaymentSaga.process(getPaymentResponse());
  }

  @Test
  void testDoublePaymentWithThreads() throws InterruptedException {
    Thread thread1 = new Thread(() -> orderPaymentSaga.process(getPaymentResponse()));
    Thread thread2 = new Thread(() -> orderPaymentSaga.process(getPaymentResponse()));

    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();

    assertPaymentOutbox();
  }

  @Test
  void testDoublePaymentWithLatch() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);

    Thread thread1 = new Thread(() -> {
      try {
        orderPaymentSaga.process(getPaymentResponse());
      } catch (OptimisticLockingFailureException e) {
        log.error("OptimisticLockingFailureException occurred for thread1");
      } finally {
        latch.countDown();
      }
    });
    Thread thread2 = new Thread(() -> {
      try {
        orderPaymentSaga.process(getPaymentResponse());
      } catch (OptimisticLockingFailureException e) {
        log.error("OptimisticLockingFailureException occurred for thread1");
      } finally {
        latch.countDown();
      }
    });

    thread1.start();
    thread2.start();
    latch.await();

    assertPaymentOutbox();

  }

  private void assertPaymentOutbox() {
    Optional<PaymentOutboxEntity> paymentOutboxEntity = paymentOutboxJpaRepository
        .findByTypeAndSagaIdAndSagaStatusIn(ORDER_SAGA_NAME, SAGA_ID,
            List.of(SagaStatus.PROCESSING));
    assertTrue(paymentOutboxEntity.isPresent());
  }

  private PaymentResponse getPaymentResponse() {
    return PaymentResponse.builder()
        .id(UUID.randomUUID().toString())
        .sagaId(SAGA_ID.toString())
        .paymentStatus(PaymentStatus.COMPLETED)
        .paymentId(PAYMENT_ID.toString())
        .orderId(ORDER_ID.toString())
        .customerId(CUSTOMER_ID.toString())
        .price(PRICE)
        .createdAt(Instant.now())
        .failureMessages(new ArrayList<>())
        .build();
  }

}
