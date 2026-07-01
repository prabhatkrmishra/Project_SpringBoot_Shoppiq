package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
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
 * Repository slice tests for {@link OrderRepository} and {@link OrderItemRepository}.
 *
 * <p>
 * Uses {@code @DataJpaTest} with an in-memory H2 database and Flyway disabled
 * (schema is created by Hibernate from entity mappings via {@code create-drop}).
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DisplayName("Order Repository Tests")
class OrderRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderItemRepository orderItemRepository;

    // ─── Helpers ──────────────────────────────────────────────────────────

    private User persistUser(String username) {
        Role role = new Role(null, "ROLE_CUSTOMER");
        role = em.persist(role);
        User user = User.builder()
                .name("Test User").username(username)
                .email(username + "@test.com").password("hashed")
                .enabled(true).roles(Set.of(role))
                .build();
        return em.persist(user);
    }

    private Address persistAddress(User user) {
        return em.persist(Address.builder()
                .user(user).label("Home").fullName("Test User")
                .phone("9999999999").line1("123 St")
                .city("City").state("ST").postalCode("12345")
                .country("IN").build());
    }

    private Order persistOrder(User user, Address address, OrderStatus status, Instant placedAt) {
        Order order = Order.builder()
                .user(user).address(address).status(status)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.PENDING)
                .subtotal(BigDecimal.valueOf(500))
                .shippingFee(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .grandTotal(BigDecimal.valueOf(500))
                .placedAt(placedAt)
                .build();
        return em.persist(order);
    }

    // ─── findAllByUserOrderByPlacedAtDesc ─────────────────────────────────

    @Test
    @DisplayName("Returns only orders belonging to the given user, newest first")
    void findAllByUser_returnsOrderedList() {
        User alice = persistUser("alice_repo");
        User bob = persistUser("bob_repo");
        Address aAddr = persistAddress(alice);
        Address bAddr = persistAddress(bob);

        Instant t1 = Instant.parse("2025-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2025-06-01T10:00:00Z");

        persistOrder(alice, aAddr, OrderStatus.PLACED, t1);
        persistOrder(alice, aAddr, OrderStatus.DELIVERED, t2);
        persistOrder(bob, bAddr, OrderStatus.PLACED, t1); // should NOT appear

        em.flush();

        List<Order> result = orderRepository.findAllByUserOrderByPlacedAtDesc(alice);

        assertThat(result).hasSize(2);
        // Newest first: t2 before t1
        assertThat(result.get(0).getPlacedAt()).isEqualTo(t2);
        assertThat(result.get(1).getPlacedAt()).isEqualTo(t1);
        result.forEach(o -> assertThat(o.getUser().getId()).isEqualTo(alice.getId()));
    }

    @Test
    @DisplayName("Returns empty list when user has no orders")
    void findAllByUser_empty() {
        User user = persistUser("nobody_repo");
        assertThat(orderRepository.findAllByUserOrderByPlacedAtDesc(user)).isEmpty();
    }

    // ─── findById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById returns present Optional for existing order")
    void findById_found() {
        User user = persistUser("find_user");
        Address addr = persistAddress(user);
        Order order = persistOrder(user, addr, OrderStatus.PLACED, Instant.now());
        em.flush();

        Optional<Order> result = orderRepository.findById(order.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(order.getId());
    }

    @Test
    @DisplayName("findById returns empty Optional for non-existent id")
    void findById_notFound() {
        assertThat(orderRepository.findById(999999L)).isEmpty();
    }

    // ─── OrderItemRepository ───────────────────────────────────────────────

    @Test
    @DisplayName("findAllByOrder returns all line items for the order")
    void findAllByOrder_returnsItems() {
        User user = persistUser("items_user");
        Address addr = persistAddress(user);
        Order order = persistOrder(user, addr, OrderStatus.PLACED, Instant.now());

        OrderItem i1 = em.persist(OrderItem.builder()
                .order(order).itemNameSnapshot("Widget A")
                .unitPriceSnapshot(BigDecimal.valueOf(100))
                .quantity(2).subtotal(BigDecimal.valueOf(200)).build());

        OrderItem i2 = em.persist(OrderItem.builder()
                .order(order).itemNameSnapshot("Widget B")
                .unitPriceSnapshot(BigDecimal.valueOf(300))
                .quantity(1).subtotal(BigDecimal.valueOf(300)).build());

        em.flush();

        List<OrderItem> items = orderItemRepository.findAllByOrder(order);

        assertThat(items).hasSize(2);
        assertThat(items).extracting(OrderItem::getItemNameSnapshot)
                .containsExactlyInAnyOrder("Widget A", "Widget B");
    }
}