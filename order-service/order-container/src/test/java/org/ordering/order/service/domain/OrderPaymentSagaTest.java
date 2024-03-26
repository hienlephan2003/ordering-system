package org.ordering.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.ordering.domain.valueobject.OrderStatus;
import org.ordering.domain.valueobject.PaymentStatus;
import org.ordering.order.service.dataaccess.order.entity.OrderEntity;
import org.ordering.order.service.dataaccess.order.repository.OrderJpaRepository;
import org.ordering.order.service.dataaccess.outbox.payment.model.PaymentOutboxEntity;
import org.ordering.order.service.dataaccess.outbox.payment.repository.PaymentOutboxJpaRepository;
import org.ordering.order.service.domain.dto.message.PaymentResponse;
import org.ordering.outbox.OutboxStatus;
import org.ordering.saga.SagaStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.ordering.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@Slf4j
@SpringBootTest(classes = OrderServiceApplication.class)
@Sql(value = {"classpath:sql/OrderPaymentSagaTestSetUp.sql"})
@Sql(value = {"classpath:sql/OrderPaymentSagaTestCleanUp.sql"}, executionPhase = AFTER_TEST_METHOD)
public class OrderPaymentSagaTest {

    @Autowired
    private OrderPaymentSaga orderPaymentSaga;

    @Autowired
    private PaymentOutboxJpaRepository paymentOutboxJpaRepository;
    @Autowired
    private OrderJpaRepository orderJpaRepository;

    private final UUID SAGA_ID = UUID.fromString("15a497c1-0f4b-4eff-b9f4-c402c8c07afa");
    private final UUID ORDER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb17");
    private final UUID CUSTOMER_ID = UUID.fromString("d215b5f8-0249-4dc5-89a3-51fd148cfb41");
    private final UUID PAYMENT_ID = UUID.randomUUID();
    private final BigDecimal PRICE = new BigDecimal("100");
    @Test()
    void textCreate(){
                Optional<List<PaymentOutboxEntity>> paymentOutboxEntity =
                paymentOutboxJpaRepository.findPaymentOutboxEntityByTypeAndOutboxStatus(
                        ORDER_SAGA_NAME,
                        OutboxStatus.STARTED
                );
    }
    @Test
    void testProcessPayment() {
//        paymentOutboxJpaRepository.save(PaymentOutboxEntity.builder()
//                        .id(UUID.randomUUID())
//                        .sagaId(SAGA_ID)
//                        .orderStatus(OrderStatus.CANCELLED)
//                        .sagaStatus(SagaStatus.COMPENSATING)
//                        .type(ORDER_SAGA_NAME)
//                        .version(1)
//                        .payload("{\"price\": 100, \"orderId\": \"ef471dac-ec22-43a7-a3f4-9d04195567a5\", \"createdAt\": \"2022-01-07T16:21:42.917756+01:00\"," +
//                        "\"customerId\": \"d215b5f8-0249-4dc5-89a3-51fd148cfb41\", \"paymentOrderStatus\": \"PENDING\"}")
//                        .outboxStatus(OutboxStatus.STARTED)
//                        .processedAt(ZonedDateTime.now())
//                        .createdAt(ZonedDateTime.now())
//                .build());

//        Optional<PaymentOutboxEntity> paymentOutboxEntity =
//                paymentOutboxJpaRepository.findPaymentOutboxEntityByTypeAndSagaIdAndSagaStatus(
//                        ORDER_SAGA_NAME,
//                        SAGA_ID,
//                        SagaStatus.COMPENSATING
//                );

//       Optional<OrderEntity> order = orderJpaRepository.findById(ORDER_ID);
//       System.out.println(paymentOutboxEntity);
//        orderPaymentSaga.process(getPaymentResponse());
//        orderPaymentSaga.process(getPaymentResponse());
    }

//    @Test
//    void testDoublePaymentWithThreads() throws InterruptedException {
//        Thread thread1 = new Thread(() -> orderPaymentSaga.process(getPaymentResponse()));
//        Thread thread2 = new Thread(() -> orderPaymentSaga.process(getPaymentResponse()));
//
//        thread1.start();
//        thread2.start();
//
//        thread1.join();
//        thread2.join();
//
//        assertPaymentOutbox();
//    }
//
//    @Test
//    void testDoublePaymentWithLatch() throws InterruptedException {
//        CountDownLatch latch = new CountDownLatch(2);
//
//        Thread thread1 = new Thread(() -> {
//            try {
//                orderPaymentSaga.process(getPaymentResponse());
//            } catch (OptimisticLockingFailureException e) {
//                log.error("OptimisticLockingFailureException occurred for thread1");
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        Thread thread2 = new Thread(() -> {
//            try {
//                orderPaymentSaga.process(getPaymentResponse());
//            } catch (OptimisticLockingFailureException e) {
//                log.error("OptimisticLockingFailureException occurred for thread2");
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        thread1.start();
//        thread2.start();
//
//        latch.await();
//
//        assertPaymentOutbox();
//
//    }

    private void assertPaymentOutbox() {
        Optional<PaymentOutboxEntity> paymentOutboxEntity =
                paymentOutboxJpaRepository.findPaymentOutboxEntityByTypeAndSagaIdAndSagaStatusIn(ORDER_SAGA_NAME, SAGA_ID,
                        List.of(SagaStatus.PROCESSING));
        assertTrue(paymentOutboxEntity.isPresent());
    }

    private PaymentResponse getPaymentResponse() {
        return PaymentResponse.builder()
                .id(UUID.randomUUID())
                .sagaId(SAGA_ID)
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentId(PAYMENT_ID)
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .price(PRICE)
                .createdAt(Instant.now())
                .failureMessages(new ArrayList<>())
                .build();
    }

}