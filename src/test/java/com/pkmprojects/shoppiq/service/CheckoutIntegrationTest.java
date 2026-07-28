package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.order.CheckoutRequest;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.order.OrderResponse;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.cart.Cart;
import com.pkmprojects.shoppiq.entity.cart.CartItem;
import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.entity.role.Role;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.general.address.AddressAccessDeniedException;
import com.pkmprojects.shoppiq.exception.general.address.AddressNotFoundException;
import com.pkmprojects.shoppiq.exception.general.cart.CartEmptyException;
import com.pkmprojects.shoppiq.exception.general.inventory.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.general.order.OrderAccessDeniedException;
import com.pkmprojects.shoppiq.repository.address.AddressRepository;
import com.pkmprojects.shoppiq.repository.cart.CartItemRepository;
import com.pkmprojects.shoppiq.repository.cart.CartRepository;
import com.pkmprojects.shoppiq.repository.category.CategoryRepository;
import com.pkmprojects.shoppiq.repository.item.ItemDetailsRepository;
import com.pkmprojects.shoppiq.repository.item.ItemRepository;
import com.pkmprojects.shoppiq.repository.order.OrderItemRepository;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.repository.payment.PaymentRepository;
import com.pkmprojects.shoppiq.repository.role.RoleRepository;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import com.pkmprojects.shoppiq.service.checkout.CheckoutServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for {@link CheckoutServiceImpl}.
 *
 * <p>Loads the full Spring application context with an H2 in-memory
 * database ({@code ddl-auto: create-drop}). All repositories, services,
 * and the payment gateway are real beans — no mocking.</p>
 *
 * <p>Covers the happy-path checkout flow end-to-end: persistence,
 * inventory reduction, cart clearing, payment creation, and email
 * dispatch (routed to console in the test profile).</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "shoppiq.ai.enabled=false",
        "AI_NVIDIA_API_KEY=test-dummy-key-for-startup"
})
@DisplayName("Checkout Integration Tests")
class CheckoutIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository rolesRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ItemDetailsRepository itemDetailsRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private CheckoutServiceImpl checkoutService;

    // ─── Shared test data ──────────────────────────────────────────────

    private User user;
    private Address address;
    private ItemDetails itemDetails;

    @BeforeEach
    void setUp() {
        // Role
        Role role = rolesRepository.findByRoleName("ROLE_CUSTOMER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName("ROLE_CUSTOMER");
                    return rolesRepository.save(r);
                });

        // User (unique email/username per test via UUID)
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        user = User.builder()
                .name("Test User " + uuid)
                .username("testuser-" + uuid)
                .email("test-" + uuid + "@test.com")
                .password("hashed-password")
                .enabled(true)
                .build();
        user.addRole(role);
        user = userRepository.save(user);

        // Address
        address = Address.builder()
                .user(user)
                .label("Home")
                .fullName("Test User")
                .phone("9999999999")
                .line1("123 Test Street")
                .city("Mumbai")
                .state("MH")
                .postalCode("400001")
                .country("India")
                .build();
        address = addressRepository.save(address);

        // Category
        Category category = Category.builder()
                .name("Electronics-" + uuid)
                .slug("electronics-" + uuid)
                .description("Test category")
                .build();
        category = categoryRepository.save(category);

        // Item + ItemDetails — Item owns the FK with CascadeType.ALL,
        // so saving Item cascades and persists ItemDetails in one transaction.
        itemDetails = ItemDetails.builder()
                .brand("TestBrand")
                .sku("SKU-" + uuid)
                .price(BigDecimal.valueOf(250.00))
                .stockQuantity(10)
                .discountPercentage(BigDecimal.ZERO)
                .category(category)
                .build();

        Item item = Item.builder()
                .name("Test Widget " + uuid)
                .slug("test-widget-" + uuid)
                .description("A test widget")
                .itemDetails(itemDetails)
                .build();
        item = itemRepository.save(item);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // checkout() — happy path
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("checkout() — successful COD order")
    class CheckoutSuccess {

        @Test
        @DisplayName("creates order, payment, reduces stock, clears cart")
        void checkout_createsOrderAndReducesStock() {
            // Arrange — cart with 2 items
            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);

            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .itemDetails(itemDetails)
                    .quantity(2)
                    .build();
            cartItemRepository.save(cartItem);

            CheckoutRequest request = new CheckoutRequest(
                    address.getId(), PaymentMethod.COD, DeliveryType.NORMAL, null);

            // Act
            CheckoutResponse response = checkoutService.checkout(user, request);

            // Assert — response
            assertThat(response.orderId()).isNotNull();
            assertThat(response.status()).isEqualTo(OrderStatus.PLACED);
            assertThat(response.grandTotal()).isEqualByComparingTo("505.00");
            assertThat(response.paymentId()).isNotNull();

            // Assert — order persisted
            Order order = orderRepository.findById(response.orderId()).orElseThrow();
            assertThat(order.getUser().getId()).isEqualTo(user.getId());
            assertThat(order.getAddress().getId()).isEqualTo(address.getId());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
            assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.COD);
            assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(order.getGrandTotal()).isEqualByComparingTo("505.00");
            assertThat(order.getPlacedAt()).isNotNull();

            // Assert — order items (via DTO to avoid LazyInitializationException)
            OrderResponse orderDetail = checkoutService.getMyOrder(user, response.orderId());
            assertThat(orderDetail.orderItems()).hasSize(1);
            assertThat(orderDetail.orderItems().getFirst().quantity()).isEqualTo(2);
            assertThat(orderDetail.orderItems().getFirst().unitPriceSnapshot()).isEqualByComparingTo("250.00");
            assertThat(orderDetail.orderItems().getFirst().subtotal()).isEqualByComparingTo("500.00");

            // Assert — stock reduced
            ItemDetails updated = itemDetailsRepository.findById(itemDetails.getId()).orElseThrow();
            assertThat(updated.getStockQuantity()).isEqualTo(8); // 10 - 2

            // Assert — cart cleared
            assertThat(cartItemRepository.findAllByCart(
                    cartRepository.findById(cart.getId()).orElseThrow())).isEmpty();

            // Assert — payment created (includes COD surcharge)
            Payment payment = paymentRepository.findById(response.paymentId()).orElseThrow();
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.COD);
            assertThat(payment.getAmount()).isEqualByComparingTo("505.00");
            assertThat(payment.getPaymentReference()).startsWith("PAY-");
        }

        @Test
        @DisplayName("multiple cart items produce correct order items and totals")
        void checkout_multipleCartItems() {
            // Second product
            String uuid2 = UUID.randomUUID().toString().substring(0, 8);
            Category category2 = categoryRepository.save(
                    Category.builder()
                            .name("Books-" + uuid2)
                            .slug("books-" + uuid2)
                            .build());

            ItemDetails details2 = ItemDetails.builder()
                    .brand("BookBrand")
                    .sku("BOOK-" + uuid2)
                    .price(BigDecimal.valueOf(100.00))
                    .stockQuantity(20)
                    .discountPercentage(BigDecimal.ZERO)
                    .category(category2)
                    .build();

            Item item2 = itemRepository.save(
                    Item.builder()
                            .name("Test Book " + uuid2)
                            .slug("test-book-" + uuid2)
                            .description("A book")
                            .itemDetails(details2)
                            .build());

            // Cart with 2 different products
            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);

            cartItemRepository.save(CartItem.builder()
                    .cart(cart).itemDetails(itemDetails).quantity(2).build()); // 2 × 250 = 500
            cartItemRepository.save(CartItem.builder()
                    .cart(cart).itemDetails(details2).quantity(3).build()); // 3 × 100 = 300

            CheckoutRequest request = new CheckoutRequest(
                    address.getId(), PaymentMethod.COD, DeliveryType.NORMAL, null);

            // Act
            CheckoutResponse response = checkoutService.checkout(user, request);

            // Assert — grand total = 500 + 300 + 5 COD = 805
            assertThat(response.grandTotal()).isEqualByComparingTo("805.00");

            // Assert — 2 order items (via DTO to avoid LazyInitializationException)
            OrderResponse orderDetail = checkoutService.getMyOrder(user, response.orderId());
            assertThat(orderDetail.orderItems()).hasSize(2);

            // Assert — both stocks reduced
            assertThat(itemDetailsRepository.findById(itemDetails.getId()).orElseThrow()
                    .getStockQuantity()).isEqualTo(8);  // 10 - 2
            assertThat(itemDetailsRepository.findById(details2.getId()).orElseThrow()
                    .getStockQuantity()).isEqualTo(17); // 20 - 3
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // checkout() — error paths
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("checkout() — error paths")
    class CheckoutErrors {

        @Test
        @DisplayName("throws CartEmptyException when cart has no items")
        void checkout_emptyCart() {
            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);

            CheckoutRequest request = new CheckoutRequest(
                    address.getId(), PaymentMethod.COD, DeliveryType.NORMAL, null);

            assertThatThrownBy(() -> checkoutService.checkout(user, request))
                    .isInstanceOf(CartEmptyException.class);
        }

        @Test
        @DisplayName("throws AddressNotFoundException when address does not exist")
        void checkout_addressNotFound() {
            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);
            cartItemRepository.save(CartItem.builder()
                    .cart(cart).itemDetails(itemDetails).quantity(1).build());

            CheckoutRequest request = new CheckoutRequest(
                    999999L, PaymentMethod.COD, DeliveryType.NORMAL, null);

            assertThatThrownBy(() -> checkoutService.checkout(user, request))
                    .isInstanceOf(AddressNotFoundException.class);
        }

        @Test
        @DisplayName("throws AddressAccessDeniedException when address belongs to another user")
        void checkout_addressWrongOwner() {
            // Create another user with their own address
            String uuid2 = UUID.randomUUID().toString().substring(0, 8);
            Role role = rolesRepository.findByRoleName("ROLE_CUSTOMER")
                    .orElseThrow();
            User other = userRepository.save(
                    User.builder()
                            .name("Other User " + uuid2)
                            .username("other-" + uuid2)
                            .email("other-" + uuid2 + "@test.com")
                            .password("hashed")
                            .enabled(true)
                            .build());
            other.addRole(role);
            other = userRepository.save(other);

            Address otherAddress = addressRepository.save(
                    Address.builder()
                            .user(other)
                            .label("Office")
                            .fullName("Other User")
                            .phone("8888888888")
                            .line1("456 Other St")
                            .city("Delhi")
                            .state("DL")
                            .postalCode("110001")
                            .country("India")
                            .build());

            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);
            cartItemRepository.save(CartItem.builder()
                    .cart(cart).itemDetails(itemDetails).quantity(1).build());

            CheckoutRequest request = new CheckoutRequest(
                    otherAddress.getId(), PaymentMethod.COD, DeliveryType.NORMAL, null);

            assertThatThrownBy(() -> checkoutService.checkout(user, request))
                    .isInstanceOf(AddressAccessDeniedException.class);
        }

        @Test
        @DisplayName("throws InsufficientStockException when requested quantity exceeds stock")
        void checkout_insufficientStock() {
            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);
            cartItemRepository.save(CartItem.builder()
                    .cart(cart).itemDetails(itemDetails).quantity(15).build()); // only 10 in stock

            CheckoutRequest request = new CheckoutRequest(
                    address.getId(), PaymentMethod.COD, DeliveryType.NORMAL, null);

            assertThatThrownBy(() -> checkoutService.checkout(user, request))
                    .isInstanceOf(InsufficientStockException.class);

            // Stock must remain unchanged
            assertThat(itemDetailsRepository.findById(itemDetails.getId()).orElseThrow()
                    .getStockQuantity()).isEqualTo(10);

            // No order should have been created
            assertThat(orderRepository.findAllByUserOrderByPlacedAtDesc(user)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getMyOrders / getMyOrder
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("order queries")
    class OrderQueries {

        @Test
        @DisplayName("getMyOrders returns orders placed by the user")
        void getMyOrders_afterCheckout() {
            // Place an order first
            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);
            cartItemRepository.save(CartItem.builder()
                    .cart(cart).itemDetails(itemDetails).quantity(1).build());

            checkoutService.checkout(user,
                    new CheckoutRequest(address.getId(), PaymentMethod.COD, DeliveryType.NORMAL, null));

            var orders = checkoutService.getMyOrders(user);
            assertThat(orders).hasSize(1);
            assertThat(orders.getFirst().status()).isEqualTo(OrderStatus.PLACED);
        }

        @Test
        @DisplayName("getMyOrder returns specific order for the owner")
        void getMyOrder_afterCheckout() {
            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);
            cartItemRepository.save(CartItem.builder()
                    .cart(cart).itemDetails(itemDetails).quantity(1).build());

            CheckoutResponse response = checkoutService.checkout(user,
                    new CheckoutRequest(address.getId(), PaymentMethod.COD, DeliveryType.NORMAL, null));

            var order = checkoutService.getMyOrder(user, response.orderId());
            assertThat(order.id()).isEqualTo(response.orderId());
            assertThat(order.grandTotal()).isEqualByComparingTo("255.00");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // cancelOrder
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        @Test
        @DisplayName("PLACED order transitions to CANCEL_REQUEST")
        void cancelOrder_placedOrder() {
            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);
            cartItemRepository.save(CartItem.builder()
                    .cart(cart).itemDetails(itemDetails).quantity(1).build());

            CheckoutResponse response = checkoutService.checkout(user,
                    new CheckoutRequest(address.getId(), PaymentMethod.COD, DeliveryType.NORMAL, null));

            checkoutService.cancelOrder(user, response.orderId());

            Order order = orderRepository.findById(response.orderId()).orElseThrow();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL_REQUEST);
        }

        @Test
        @DisplayName("wrong owner cannot cancel")
        void cancelOrder_wrongOwner() {
            Cart cart = Cart.builder().user(user).build();
            cart = cartRepository.save(cart);
            cartItemRepository.save(CartItem.builder()
                    .cart(cart).itemDetails(itemDetails).quantity(1).build());

            CheckoutResponse checkoutResponse = checkoutService.checkout(user,
                    new CheckoutRequest(address.getId(), PaymentMethod.COD, DeliveryType.NORMAL, null));
            Long orderId = checkoutResponse.orderId();
            String uuid2 = UUID.randomUUID().toString().substring(0, 8);
            Role role = rolesRepository.findByRoleName("ROLE_CUSTOMER").orElseThrow();
            User other = userRepository.save(
                    User.builder()
                            .name("Other " + uuid2)
                            .username("other-" + uuid2)
                            .email("other-" + uuid2 + "@test.com")
                            .password("hashed")
                            .enabled(true)
                            .build());
            other.addRole(role);
            User savedOther = userRepository.save(other);

            assertThatThrownBy(() -> checkoutService.cancelOrder(savedOther, orderId))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }
    }
}
