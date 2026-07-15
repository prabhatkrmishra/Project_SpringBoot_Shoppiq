package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.order.CheckoutRequest;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.order.OrderResponse;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.OrderEmailService;
import com.pkmprojects.shoppiq.service.PaymentService;
import com.pkmprojects.shoppiq.service.impl.CheckoutServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CheckoutServiceImpl}.
 *
 * <p>All repositories are mocked. No Spring context or database involved.</p>
 *
 * <h2>Coverage</h2>
 * <ul>
 *   <li>checkout — success, empty cart, missing cart, invalid address,
 *       address ownership, insufficient stock</li>
 *   <li>checkout — order creation, inventory reduction, cart clearing</li>
 *   <li>getMyOrders — delegates to repository</li>
 *   <li>getMyOrder — found, not found, wrong owner</li>
 *   <li>cancelOrder — success, wrong owner, not cancellable status</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutServiceImpl Tests")
class CheckoutServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemDetailsRepository itemDetailsRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PromoCodeService promoCodeService;
    @Mock
    private OrderEmailService orderEmailService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    // ─── Helpers ──────────────────────────────────────────────────────────

    private static void setId(Object entity, Long id) throws Exception {
        // AuditableEntity → BaseEntity → id
        Field field = entity.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private static void setOrderItemId(OrderItem item, Long id) throws Exception {
        Field f = OrderItem.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(item, id);
    }

    private User buildUser(long id) throws Exception {
        User user = User.builder()
                .name("Alice").username("alice")
                .email("alice@test.com").password("hashed")
                .enabled(true).build();
        setId(user, id);
        return user;
    }

    private Address buildAddress(long id, User owner) throws Exception {
        Address address = Address.builder()
                .user(owner).label("Home").fullName("Alice")
                .phone("9999999999").line1("123 Main St")
                .city("Mumbai").state("MH").postalCode("400001")
                .country("India").build();
        setId(address, id);
        return address;
    }

    private ItemDetails buildItemDetails(long id, BigDecimal price, int stock) throws Exception {
        Item item = Item.builder()
                .name("Widget").description("A widget").build();
        setId(item, id * 10);

        ItemDetails details = ItemDetails.builder()
                .brand("Brand").sku("SKU-" + id)
                .price(price).stockQuantity(stock)
                .discountPercentage(BigDecimal.ZERO)
                .item(item)
                .build();
        setId(details, id);
        item.setItemDetails(details);
        return details;
    }

    private CartItem buildCartItem(ItemDetails details, int quantity) {
        return CartItem.builder()
                .itemDetails(details)
                .quantity(quantity)
                .build();
    }

    private Cart buildCart(User user, List<CartItem> items) throws Exception {
        Cart cart = Cart.builder().user(user).build();
        setId(cart, 1L);
        for (CartItem ci : items) {
            cart.addItem(ci);
        }
        return cart;
    }

    private Order buildOrder(long id, User user, Address address, OrderStatus status) throws Exception {
        Order order = Order.builder()
                .user(user).address(address)
                .status(status)
                .paymentMethod(PaymentMethod.COD)
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

    // ═══════════════════════════════════════════════════════════════════════
    // checkout()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("checkout()")
    class CheckoutTests {

        @Test
        @DisplayName("Success — order created, inventory reduced, cart cleared")
        void checkout_success() throws Exception {
            // arrange
            User user = buildUser(1L);
            Address address = buildAddress(5L, user);
            ItemDetails details = buildItemDetails(10L, BigDecimal.valueOf(250), 5);

            CartItem cartItem = buildCartItem(details, 2);
            Cart cart = buildCart(user, List.of(cartItem));

            CheckoutRequest request = new CheckoutRequest(5L, PaymentMethod.COD, null);

            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
            when(addressRepository.findById(5L)).thenReturn(Optional.of(address));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                setId(o, 99L);
                return o;
            });
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(itemDetailsRepository.save(any(ItemDetails.class))).thenAnswer(inv -> inv.getArgument(0));

            Payment payment = Payment.builder().build();
            setId(payment, 77L);
            when(paymentService.createPayment(any(Order.class))).thenReturn(payment);

            // act
            CheckoutResponse response = checkoutService.checkout(user, request);

            // assert — response
            assertThat(response.orderId()).isEqualTo(99L);
            assertThat(response.status()).isEqualTo(OrderStatus.PLACED);
            assertThat(response.paymentId()).isEqualTo(77L);
            assertThat(response.grandTotal()).isEqualByComparingTo("500.00");

            // assert — inventory reduced
            assertThat(details.getStockQuantity()).isEqualTo(3); // 5 - 2

            // assert — cart cleared
            assertThat(cart.getItems()).isEmpty();

            verify(orderRepository).save(any(Order.class));
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("Fails — no cart exists (CartEmptyException)")
        void checkout_noCart_throws() throws Exception {
            User user = buildUser(1L);
            when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    checkoutService.checkout(user, new CheckoutRequest(1L, PaymentMethod.COD, null))
            ).isInstanceOf(CartEmptyException.class);
        }

        @Test
        @DisplayName("Fails — cart is present but empty (CartEmptyException)")
        void checkout_emptyCart_throws() throws Exception {
            User user = buildUser(1L);
            Cart cart = buildCart(user, new ArrayList<>());
            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

            assertThatThrownBy(() ->
                    checkoutService.checkout(user, new CheckoutRequest(1L, PaymentMethod.COD, null))
            ).isInstanceOf(CartEmptyException.class);
        }

        @Test
        @DisplayName("Fails — address not found (AddressNotFoundException)")
        void checkout_addressNotFound_throws() throws Exception {
            User user = buildUser(1L);
            ItemDetails details = buildItemDetails(1L, BigDecimal.valueOf(100), 10);
            Cart cart = buildCart(user, List.of(buildCartItem(details, 1)));

            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
            when(addressRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    checkoutService.checkout(user, new CheckoutRequest(99L, PaymentMethod.COD, null))
            ).isInstanceOf(AddressNotFoundException.class);
        }

        @Test
        @DisplayName("Fails — address belongs to different user (AddressAccessDeniedException)")
        void checkout_addressWrongOwner_throws() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Address bobsAddress = buildAddress(5L, bob);
            ItemDetails details = buildItemDetails(1L, BigDecimal.valueOf(100), 10);
            Cart cart = buildCart(alice, List.of(buildCartItem(details, 1)));

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(cart));
            when(addressRepository.findById(5L)).thenReturn(Optional.of(bobsAddress));

            assertThatThrownBy(() ->
                    checkoutService.checkout(alice, new CheckoutRequest(5L, PaymentMethod.COD, null))
            ).isInstanceOf(AddressAccessDeniedException.class);
        }

        @Test
        @DisplayName("Fails — insufficient stock (InsufficientStockException)")
        void checkout_insufficientStock_throws() throws Exception {
            User user = buildUser(1L);
            Address address = buildAddress(5L, user);
            ItemDetails details = buildItemDetails(1L, BigDecimal.valueOf(100), 1); // only 1 in stock

            CartItem cartItem = buildCartItem(details, 3); // requesting 3
            Cart cart = buildCart(user, List.of(cartItem));

            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
            when(addressRepository.findById(5L)).thenReturn(Optional.of(address));

            assertThatThrownBy(() ->
                    checkoutService.checkout(user, new CheckoutRequest(5L, PaymentMethod.COD, null))
            ).isInstanceOf(InsufficientStockException.class);

            // Inventory must NOT have been modified
            assertThat(details.getStockQuantity()).isEqualTo(1);
            verifyNoInteractions(orderRepository);
        }

        @Test
        @DisplayName("Success — grand total computed correctly across multiple items")
        void checkout_correctTotalCalculation() throws Exception {
            User user = buildUser(1L);
            Address address = buildAddress(5L, user);
            ItemDetails d1 = buildItemDetails(1L, BigDecimal.valueOf(100), 10);
            ItemDetails d2 = buildItemDetails(2L, BigDecimal.valueOf(200), 10);

            Cart cart = buildCart(user, List.of(
                    buildCartItem(d1, 2), // 200
                    buildCartItem(d2, 3)  // 600 → total 800
            ));

            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
            when(addressRepository.findById(5L)).thenReturn(Optional.of(address));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                setId(o, 1L);
                return o;
            });
            when(cartRepository.save(any())).thenReturn(cart);
            when(itemDetailsRepository.save(any(ItemDetails.class))).thenAnswer(inv -> inv.getArgument(0));

            Payment payment = Payment.builder().build();
            setId(payment, 42L);
            when(paymentService.createPayment(any(Order.class))).thenReturn(payment);

            CheckoutResponse response = checkoutService.checkout(user, new CheckoutRequest(5L, PaymentMethod.COD, null));

            assertThat(response.grandTotal()).isEqualByComparingTo("800.00");
            assertThat(response.paymentId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("Fails — concurrent stock modification (StockConflictException)")
        void checkout_concurrentModification_throws() throws Exception {
            User user = buildUser(1L);
            Address address = buildAddress(5L, user);
            ItemDetails details = buildItemDetails(10L, BigDecimal.valueOf(250), 5);

            CartItem cartItem = buildCartItem(details, 2);
            Cart cart = buildCart(user, List.of(cartItem));

            CheckoutRequest request = new CheckoutRequest(5L, PaymentMethod.COD, null);

            when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
            when(addressRepository.findById(5L)).thenReturn(Optional.of(address));
            when(itemDetailsRepository.save(any(ItemDetails.class)))
                    .thenThrow(new org.springframework.dao.OptimisticLockingFailureException("version mismatch"));

            assertThatThrownBy(() ->
                    checkoutService.checkout(user, request)
            ).isInstanceOf(StockConflictException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getMyOrders()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMyOrders()")
    class GetMyOrdersTests {

        @Test
        @DisplayName("Returns mapped list from repository")
        void getMyOrders_returnsList() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order o1 = buildOrder(1L, user, addr, OrderStatus.PLACED);
            Order o2 = buildOrder(2L, user, addr, OrderStatus.DELIVERED);

            when(orderRepository.findAllByUserOrderByPlacedAtDesc(user))
                    .thenReturn(List.of(o1, o2));

            List<OrderResponse> result = checkoutService.getMyOrders(user);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Returns empty list when user has no orders")
        void getMyOrders_empty() throws Exception {
            User user = buildUser(1L);
            when(orderRepository.findAllByUserOrderByPlacedAtDesc(user))
                    .thenReturn(List.of());

            assertThat(checkoutService.getMyOrders(user)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getMyOrder()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getMyOrder()")
    class GetMyOrderTests {

        @Test
        @DisplayName("Returns order response when order belongs to user")
        void getMyOrder_found() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.PLACED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            OrderResponse response = checkoutService.getMyOrder(user, 10L);

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.status()).isEqualTo(OrderStatus.PLACED);
        }

        @Test
        @DisplayName("Throws OrderNotFoundException when order doesn't exist")
        void getMyOrder_notFound() throws Exception {
            User user = buildUser(1L);
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> checkoutService.getMyOrder(user, 99L))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("Throws OrderAccessDeniedException when order belongs to another user")
        void getMyOrder_wrongOwner() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Address addr = buildAddress(1L, bob);
            Order order = buildOrder(10L, bob, addr, OrderStatus.PLACED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.getMyOrder(alice, 10L))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // cancelOrder()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrderTests {

        @Test
        @DisplayName("Success — PLACED order is set to CANCEL_REQUEST")
        void cancelOrder_success() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.PLACED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            checkoutService.cancelOrder(user, 10L);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL_REQUEST);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Fails — order not found (OrderNotFoundException)")
        void cancelOrder_notFound() throws Exception {
            User user = buildUser(1L);
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> checkoutService.cancelOrder(user, 99L))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("Fails — order belongs to another user (OrderAccessDeniedException)")
        void cancelOrder_wrongOwner() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Address addr = buildAddress(1L, bob);
            Order order = buildOrder(10L, bob, addr, OrderStatus.PLACED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.cancelOrder(alice, 10L))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }

        @Test
        @DisplayName("Fails — SHIPPED order cannot be cancelled (OrderCannotBeCancelledException)")
        void cancelOrder_shipped_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.SHIPPED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.cancelOrder(user, 10L))
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }

        @Test
        @DisplayName("Fails — DELIVERED order cannot be cancelled")
        void cancelOrder_delivered_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.cancelOrder(user, 10L))
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }

        @Test
        @DisplayName("Fails — CANCELLED order cannot be cancelled again")
        void cancelOrder_alreadyCancelled_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.CANCELLED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.cancelOrder(user, 10L))
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }

        @Test
        @DisplayName("Fails — CONFIRMED order cannot be cancelled")
        void cancelOrder_confirmed_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.CONFIRMED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.cancelOrder(user, 10L))
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }

        @Test
        @DisplayName("Fails — OUT_FOR_DELIVERY order cannot be cancelled")
        void cancelOrder_outForDelivery_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.OUT_FOR_DELIVERY);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.cancelOrder(user, 10L))
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }

        @Test
        @DisplayName("Fails — RETURN_REQUEST order cannot be cancelled")
        void cancelOrder_returnRequest_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.RETURN_REQUEST);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.cancelOrder(user, 10L))
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }

        @Test
        @DisplayName("Fails — REFUND_REQUEST order cannot be cancelled")
        void cancelOrder_refundRequest_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.REFUND_REQUEST);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.cancelOrder(user, 10L))
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }

        @Test
        @DisplayName("Fails — CANCEL_REQUEST order cannot be cancelled again")
        void cancelOrder_cancelRequest_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.CANCEL_REQUEST);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.cancelOrder(user, 10L))
                    .isInstanceOf(OrderCannotBeCancelledException.class);
        }

        @Test
        @DisplayName("save is called exactly once for successful cancel")
        void cancelOrder_savesExactlyOnce() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.PLACED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            checkoutService.cancelOrder(user, 10L);

            verify(orderRepository, times(1)).save(order);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // requestReturn()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("requestReturn()")
    class RequestReturnTests {

        @Test
        @DisplayName("Success — DELIVERED order is set to RETURN_REQUEST")
        void requestReturn_success() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            checkoutService.requestReturn(user, 10L);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.RETURN_REQUEST);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Fails — PLACED order cannot request return")
        void requestReturn_placed_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.PLACED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReturn(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — order not found")
        void requestReturn_notFound() throws Exception {
            User user = buildUser(1L);
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> checkoutService.requestReturn(user, 99L))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("Fails — wrong owner")
        void requestReturn_wrongOwner() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Address addr = buildAddress(1L, bob);
            Order order = buildOrder(10L, bob, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReturn(alice, 10L))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }

        @Test
        @DisplayName("Fails — SHIPPED order cannot request return")
        void requestReturn_shipped_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.SHIPPED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReturn(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — CANCELLED order cannot request return")
        void requestReturn_cancelled_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.CANCELLED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReturn(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — CONFIRMED order cannot request return")
        void requestReturn_confirmed_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.CONFIRMED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReturn(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — RETURNED order cannot request return again")
        void requestReturn_alreadyReturned_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.RETURNED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReturn(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — RETURN_REQUEST order cannot request return again")
        void requestReturn_alreadyReturnRequest_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.RETURN_REQUEST);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReturn(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("save is called exactly once for successful return request")
        void requestReturn_savesExactlyOnce() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            checkoutService.requestReturn(user, 10L);

            verify(orderRepository, times(1)).save(order);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // requestRefund()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("requestRefund()")
    class RequestRefundTests {

        @Test
        @DisplayName("Success — DELIVERED order is set to REFUND_REQUEST")
        void requestRefund_success() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            checkoutService.requestRefund(user, 10L);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUND_REQUEST);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Fails — SHIPPED order cannot request refund")
        void requestRefund_shipped_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.SHIPPED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestRefund(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — order not found")
        void requestRefund_notFound() throws Exception {
            User user = buildUser(1L);
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> checkoutService.requestRefund(user, 99L))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("Fails — wrong owner")
        void requestRefund_wrongOwner() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Address addr = buildAddress(1L, bob);
            Order order = buildOrder(10L, bob, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestRefund(alice, 10L))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }

        @Test
        @DisplayName("Fails — PLACED order cannot request refund")
        void requestRefund_placed_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.PLACED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestRefund(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — CANCELLED order cannot request refund")
        void requestRefund_cancelled_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.CANCELLED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestRefund(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — CONFIRMED order cannot request refund")
        void requestRefund_confirmed_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.CONFIRMED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestRefund(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — REFUNDED order cannot request refund again")
        void requestRefund_alreadyRefunded_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.REFUNDED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestRefund(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — REFUND_REQUEST order cannot request refund again")
        void requestRefund_alreadyRefundRequest_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.REFUND_REQUEST);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestRefund(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("save is called exactly once for successful refund request")
        void requestRefund_savesExactlyOnce() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            checkoutService.requestRefund(user, 10L);

            verify(orderRepository, times(1)).save(order);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // requestReplacement()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("requestReplacement()")
    class RequestReplacementTests {

        @Test
        @DisplayName("Success — DELIVERED order is set to REPLACE_REQUEST")
        void requestReplacement_success() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            checkoutService.requestReplacement(user, 10L);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.REPLACE_REQUEST);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Fails — CANCELLED order cannot request replacement")
        void requestReplacement_cancelled_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.CANCELLED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReplacement(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — order not found")
        void requestReplacement_notFound() throws Exception {
            User user = buildUser(1L);
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> checkoutService.requestReplacement(user, 99L))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("Fails — wrong owner")
        void requestReplacement_wrongOwner() throws Exception {
            User alice = buildUser(1L);
            User bob = buildUser(2L);
            Address addr = buildAddress(1L, bob);
            Order order = buildOrder(10L, bob, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReplacement(alice, 10L))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }

        @Test
        @DisplayName("Fails — PLACED order cannot request replacement")
        void requestReplacement_placed_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.PLACED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReplacement(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — SHIPPED order cannot request replacement")
        void requestReplacement_shipped_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.SHIPPED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReplacement(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — CONFIRMED order cannot request replacement")
        void requestReplacement_confirmed_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.CONFIRMED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReplacement(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — REPLACED order cannot request replacement again")
        void requestReplacement_alreadyReplaced_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.REPLACED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReplacement(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — REPLACE_REQUEST order cannot request replacement again")
        void requestReplacement_alreadyReplaceRequest_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.REPLACE_REQUEST);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReplacement(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("Fails — RETURNED order cannot request replacement")
        void requestReplacement_returned_throws() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.RETURNED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> checkoutService.requestReplacement(user, 10L))
                    .isInstanceOf(OrderInvalidStatusTransitionException.class);
        }

        @Test
        @DisplayName("save is called exactly once for successful replacement request")
        void requestReplacement_savesExactlyOnce() throws Exception {
            User user = buildUser(1L);
            Address addr = buildAddress(1L, user);
            Order order = buildOrder(10L, user, addr, OrderStatus.DELIVERED);

            when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

            checkoutService.requestReplacement(user, 10L);

            verify(orderRepository, times(1)).save(order);
        }
    }
}