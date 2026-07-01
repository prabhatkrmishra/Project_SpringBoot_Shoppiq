package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentGateway;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice tests for {@link PaymentRepository}.
 *
 * <p>Uses H2 in-memory DB with Flyway disabled and Hibernate schema create-drop.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DisplayName("PaymentRepository Tests")
class PaymentRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    PaymentRepository paymentRepository;

    // ─── Helpers ──────────────────────────────────────────────────────────

    private User persistUser(String username) {
        Role role = new Role(null, "ROLE_CUSTOMER");
        role = em.persist(role);
        User user = User.builder()
                .name("Test").username(username)
                .email(username + "@test.com").password("hashed")
                .enabled(true).roles(Set.of(role)).build();
        return em.persist(user);
    }

    private Address persistAddress(User user) {
        return em.persist(Address.builder()
                .user(user).label("Home").fullName("Test")
                .phone("9999999999").line1("123 St")
                .city("City").state("ST").postalCode("12345")
                .country("IN").build());
    }

    private Order persistOrder(User user) {
        Address addr = persistAddress(user);
        return em.persist(Order.builder()
                .user(user).address(addr)
                .status(OrderStatus.PLACED)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.PENDING)
                .subtotal(BigDecimal.valueOf(500))
                .shippingFee(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .grandTotal(BigDecimal.valueOf(500))
                .placedAt(Instant.now())
                .build());
    }

    private Payment persistPayment(Order order, String ref, String txnId, PaymentStatus status) {
        return em.persist(Payment.builder()
                .order(order)
                .paymentReference(ref)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(status)
                .gateway(PaymentGateway.NONE)
                .amount(BigDecimal.valueOf(500))
                .currency("INR")
                .transactionId(txnId)
                .build());
    }

    // ─── findByOrder ──────────────────────────────────────────────────────

    @Test
    @DisplayName("findByOrder returns the payment for the given order")
    void findByOrder_returnsPayment() {
        User user = persistUser("order_user");
        Order order = persistOrder(user);
        Payment payment = persistPayment(order, "PAY-REF-001", null, PaymentStatus.PENDING);
        em.flush();

        Optional<Payment> result = paymentRepository.findByOrder(order);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("findByOrder returns empty when no payment exists for order")
    void findByOrder_empty() {
        User user = persistUser("noPayment_user");
        Order order = persistOrder(user);
        em.flush();

        assertThat(paymentRepository.findByOrder(order)).isEmpty();
    }

    // ─── findByPaymentReference ───────────────────────────────────────────

    @Test
    @DisplayName("findByPaymentReference returns correct payment")
    void findByPaymentReference_found() {
        User user = persistUser("ref_user");
        Order order = persistOrder(user);
        persistPayment(order, "PAY-UNIQUE-REF", null, PaymentStatus.PENDING);
        em.flush();

        Optional<Payment> result = paymentRepository.findByPaymentReference("PAY-UNIQUE-REF");

        assertThat(result).isPresent();
        assertThat(result.get().getPaymentReference()).isEqualTo("PAY-UNIQUE-REF");
    }

    @Test
    @DisplayName("findByPaymentReference returns empty for unknown reference")
    void findByPaymentReference_notFound() {
        assertThat(paymentRepository.findByPaymentReference("NO-SUCH-REF")).isEmpty();
    }

    // ─── findByTransactionId ──────────────────────────────────────────────

    @Test
    @DisplayName("findByTransactionId returns correct payment")
    void findByTransactionId_found() {
        User user = persistUser("txn_user");
        Order order = persistOrder(user);
        persistPayment(order, "PAY-TXN-001", "TXN-ABCDEF", PaymentStatus.PAID);
        em.flush();

        Optional<Payment> result = paymentRepository.findByTransactionId("TXN-ABCDEF");

        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-ABCDEF");
    }

    @Test
    @DisplayName("findByTransactionId returns empty for unknown transaction")
    void findByTransactionId_notFound() {
        assertThat(paymentRepository.findByTransactionId("NO-TXN")).isEmpty();
    }

    // ─── existsByOrder ─────────────────────────────────────────────────────

    @Test
    @DisplayName("existsByOrder returns true when payment exists")
    void existsByOrder_true() {
        User user = persistUser("exist_user");
        Order order = persistOrder(user);
        persistPayment(order, "PAY-EXIST-001", null, PaymentStatus.PENDING);
        em.flush();

        assertThat(paymentRepository.existsByOrder(order)).isTrue();
    }

    @Test
    @DisplayName("existsByOrder returns false when no payment exists")
    void existsByOrder_false() {
        User user = persistUser("noExist_user");
        Order order = persistOrder(user);
        em.flush();

        assertThat(paymentRepository.existsByOrder(order)).isFalse();
    }
}