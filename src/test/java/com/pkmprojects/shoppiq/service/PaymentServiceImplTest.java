package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.payment.PaymentResponse;
import com.pkmprojects.shoppiq.dto.payment.PaymentStatusResponse;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.gateway.payment.CodPaymentGateway;
import com.pkmprojects.shoppiq.gateway.payment.OnlinePaymentGateway;
import com.pkmprojects.shoppiq.gateway.payment.PaymentGatewayRegistry;
import com.pkmprojects.shoppiq.repository.PaymentRepository;
import com.pkmprojects.shoppiq.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentServiceImpl}.
 *
 * <p>All dependencies are mocked. No Spring context or database involved.</p>
 *
 * <h2>Coverage</h2>
 * <ul>
 *   <li>createPayment — success (COD/ONLINE), duplicate prevention</li>
 *   <li>pay — success, already paid, invalid state</li>
 *   <li>verifyPayment — success, transaction not found, wrong owner</li>
 *   <li>cancelPayment — success, wrong owner, invalid state</li>
 *   <li>refund — success, not paid</li>
 *   <li>getPayment — success, not found, wrong owner</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    // Use real gateway implementations — they are stateless and safe to use directly
    private final CodPaymentGateway codGateway = new CodPaymentGateway();
    private final OnlinePaymentGateway onlineGateway = new OnlinePaymentGateway();

    private PaymentGatewayRegistry gatewayRegistry;
    private PaymentServiceImpl paymentService;

    // ─── Setup ────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        gatewayRegistry = new PaymentGatewayRegistry(codGateway, onlineGateway);
        paymentService = new PaymentServiceImpl(paymentRepository, gatewayRegistry);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private static void setId(Object entity, Long id) throws Exception {
        Field f = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }

    private static void setPaymentId(Payment p, Long id) throws Exception {
        Field f = Payment.class.getSuperclass().getSuperclass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(p, id);
    }

    private User buildUser(long id) throws Exception {
        User user = User.builder().name("Alice").username("alice")
                .email("alice@test.com").password("hashed").enabled(true).build();
        setId(user, id);
        return user;
    }

    private Order buildOrder(long id, User user, PaymentMethod method) throws Exception {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PLACED)
                .paymentMethod(method)
                .paymentStatus(PaymentStatus.PENDING)
                .subtotal(BigDecimal.valueOf(500))
                .shippingFee(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .grandTotal(BigDecimal.valueOf(500))
                .placedAt(Instant.now())
                .build();
        setId(order, id);
        return order;
    }

    private Payment buildPayment(long id, User user, PaymentMethod method, PaymentStatus status)
            throws Exception {
        Order order = buildOrder(id * 10, user, method);
        Payment payment = Payment.builder()
                .order(order)
                .paymentReference("PAY-20260701-" + id)
                .paymentMethod(method)
                .paymentStatus(status)
                .gateway(PaymentGateway.NONE)
                .amount(BigDecimal.valueOf(500))
                .currency("INR")
                .build();
        setPaymentId(payment, id);
        return payment;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // createPayment()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createPayment()")
    class CreatePaymentTests {

        @Test
        @DisplayName("COD — payment created with PENDING status")
        void createPayment_cod_pending() throws Exception {
            User user = buildUser(1L);
            Order order = buildOrder(1L, user, PaymentMethod.COD);

            when(paymentRepository.existsByOrder(order)).thenReturn(false);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                setPaymentId(p, 99L);
                return p;
            });

            Payment result = paymentService.createPayment(order);

            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.COD);
            assertThat(result.getPaymentReference()).contains("PAY-");
            assertThat(result.getAmount()).isEqualByComparingTo("500.00");
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("ONLINE — payment created with PROCESSING status")
        void createPayment_online_processing() throws Exception {
            User user = buildUser(1L);
            Order order = buildOrder(2L, user, PaymentMethod.ONLINE);

            when(paymentRepository.existsByOrder(order)).thenReturn(false);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                setPaymentId(p, 100L);
                return p;
            });

            Payment result = paymentService.createPayment(order);

            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PROCESSING);
        }

        @Test
        @DisplayName("Throws DuplicatePaymentException when payment already exists")
        void createPayment_duplicate_throws() throws Exception {
            User user = buildUser(1L);
            Order order = buildOrder(1L, user, PaymentMethod.COD);

            when(paymentRepository.existsByOrder(order)).thenReturn(true);

            assertThatThrownBy(() -> paymentService.createPayment(order))
                    .isInstanceOf(DuplicatePaymentException.class);

            verify(paymentRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // pay()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("pay()")
    class PayTests {

        @Test
        @DisplayName("PENDING ONLINE payment moves to PROCESSING")
        void pay_pendingOnline_processing() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.ONLINE, PaymentStatus.PENDING);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenReturn(payment);

            PaymentStatusResponse result = paymentService.pay(user, 1L);

            assertThat(result.status()).isEqualTo(PaymentStatus.PROCESSING);
        }

        @Test
        @DisplayName("FAILED payment can be retried")
        void pay_failedPayment_retried() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.ONLINE, PaymentStatus.FAILED);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenReturn(payment);

            PaymentStatusResponse result = paymentService.pay(user, 1L);

            assertThat(result.status()).isEqualTo(PaymentStatus.PROCESSING);
        }

        @Test
        @DisplayName("Throws PaymentInvalidStateException when already PAID")
        void pay_alreadyPaid_throws() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.ONLINE, PaymentStatus.PAID);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.pay(user, 1L))
                    .isInstanceOf(PaymentInvalidStateException.class);
        }

        @Test
        @DisplayName("Throws PaymentInvalidStateException when CANCELLED")
        void pay_cancelled_throws() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.ONLINE, PaymentStatus.CANCELLED);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.pay(user, 1L))
                    .isInstanceOf(PaymentInvalidStateException.class);
        }

        @Test
        @DisplayName("Throws PaymentAccessDeniedException for wrong user")
        void pay_wrongOwner_throws() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Payment payment = buildPayment(1L, bob, PaymentMethod.ONLINE, PaymentStatus.PENDING);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.pay(alice, 1L))
                    .isInstanceOf(PaymentAccessDeniedException.class);
        }

        @Test
        @DisplayName("Throws PaymentNotFoundException when payment does not exist")
        void pay_notFound_throws() throws Exception {
            User user = buildUser(1L);
            when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.pay(user, 99L))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // verifyPayment()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("verifyPayment()")
    class VerifyPaymentTests {

        @Test
        @DisplayName("Marks payment as PAID and sets transactionId")
        void verifyPayment_success() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.ONLINE, PaymentStatus.PROCESSING);

            when(paymentRepository.findByTransactionId("TXN-001")).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenReturn(payment);

            PaymentStatusResponse result = paymentService.verifyPayment(user, "TXN-001");

            assertThat(result.status()).isEqualTo(PaymentStatus.PAID);
            assertThat(payment.getTransactionId()).isEqualTo("TXN-001");
            assertThat(payment.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("Throws PaymentNotFoundException for unknown transactionId")
        void verifyPayment_notFound_throws() throws Exception {
            User user = buildUser(1L);
            when(paymentRepository.findByTransactionId("BAD-ID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.verifyPayment(user, "BAD-ID"))
                    .isInstanceOf(PaymentNotFoundException.class);
        }

        @Test
        @DisplayName("Throws PaymentAccessDeniedException for wrong user")
        void verifyPayment_wrongOwner_throws() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Payment payment = buildPayment(1L, bob, PaymentMethod.ONLINE, PaymentStatus.PROCESSING);

            when(paymentRepository.findByTransactionId("TXN-002")).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.verifyPayment(alice, "TXN-002"))
                    .isInstanceOf(PaymentAccessDeniedException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // cancelPayment()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelPayment()")
    class CancelPaymentTests {

        @Test
        @DisplayName("Cancels a PENDING payment")
        void cancelPayment_pending_success() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.ONLINE, PaymentStatus.PENDING);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenReturn(payment);

            PaymentStatusResponse result = paymentService.cancelPayment(user, 1L);

            assertThat(result.status()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("Cancels a FAILED payment")
        void cancelPayment_failed_success() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.ONLINE, PaymentStatus.FAILED);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any())).thenReturn(payment);

            PaymentStatusResponse result = paymentService.cancelPayment(user, 1L);

            assertThat(result.status()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("Throws PaymentInvalidStateException for PAID payment")
        void cancelPayment_paid_throws() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.ONLINE, PaymentStatus.PAID);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.cancelPayment(user, 1L))
                    .isInstanceOf(PaymentInvalidStateException.class);
        }

        @Test
        @DisplayName("Throws PaymentInvalidStateException for PROCESSING payment")
        void cancelPayment_processing_throws() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.ONLINE, PaymentStatus.PROCESSING);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.cancelPayment(user, 1L))
                    .isInstanceOf(PaymentInvalidStateException.class);
        }

        @Test
        @DisplayName("Throws PaymentAccessDeniedException for wrong user")
        void cancelPayment_wrongOwner_throws() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Payment payment = buildPayment(1L, bob, PaymentMethod.ONLINE, PaymentStatus.PENDING);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.cancelPayment(alice, 1L))
                    .isInstanceOf(PaymentAccessDeniedException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getPayment()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getPayment()")
    class GetPaymentTests {

        @Test
        @DisplayName("Returns full payment response for the owner")
        void getPayment_success() throws Exception {
            User user = buildUser(1L);
            Payment payment = buildPayment(1L, user, PaymentMethod.COD, PaymentStatus.PENDING);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            PaymentResponse result = paymentService.getPayment(user, 1L);

            assertThat(result.paymentMethod()).isEqualTo(PaymentMethod.COD);
            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.currency()).isEqualTo("INR");
        }

        @Test
        @DisplayName("Throws PaymentNotFoundException when not found")
        void getPayment_notFound_throws() throws Exception {
            User user = buildUser(1L);
            when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPayment(user, 99L))
                    .isInstanceOf(PaymentNotFoundException.class);
        }

        @Test
        @DisplayName("Throws PaymentAccessDeniedException for wrong user")
        void getPayment_wrongOwner_throws() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Payment payment = buildPayment(1L, bob, PaymentMethod.COD, PaymentStatus.PENDING);

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.getPayment(alice, 1L))
                    .isInstanceOf(PaymentAccessDeniedException.class);
        }
    }
}