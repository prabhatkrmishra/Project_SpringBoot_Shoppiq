package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.request.AddCartItemRequest;
import com.pkmprojects.shoppiq.dto.response.CartItemResponse;
import com.pkmprojects.shoppiq.dto.response.CartResponse;
import com.pkmprojects.shoppiq.dto.request.UpdateCartItemRequest;
import com.pkmprojects.shoppiq.entity.Cart;
import com.pkmprojects.shoppiq.entity.CartItem;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemDetails;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.CartItemAccessDeniedException;
import com.pkmprojects.shoppiq.exception.CartItemNotFoundException;
import com.pkmprojects.shoppiq.exception.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.ItemDetailsNotFoundException;
import com.pkmprojects.shoppiq.repository.CartItemRepository;
import com.pkmprojects.shoppiq.repository.CartRepository;
import com.pkmprojects.shoppiq.repository.ItemDetailsRepository;
import com.pkmprojects.shoppiq.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CartServiceImpl}.
 *
 * <p>All dependencies are mocked. No Spring context or database is involved.</p>
 *
 * <h2>Coverage</h2>
 * <ul>
 *     <li>create() — happy path, lazy cart creation, duplicate merge, stock
 *     validation, item-not-found</li>
 *     <li>get() — full cart, empty cart</li>
 *     <li>getById() — found, not found, wrong owner</li>
 *     <li>update() — success, stock exceeded, not found, wrong owner</li>
 *     <li>delete() — success, not found, wrong owner</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl Tests")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemDetailsRepository itemDetailsRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Reflectively sets the private {@code id} field declared in
     * {@code BaseEntity} on any {@code AuditableEntity} subclass.
     */
    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    /**
     * Sets the id on a {@link CartItem}, which does not extend AuditableEntity.
     */
    private static void setCartItemId(CartItem cartItem, Long id) throws Exception {
        Field field = CartItem.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(cartItem, id);
    }

    /**
     * Builds a User with id=1, username=alice.
     */
    private User buildUser(long id) throws Exception {
        User user = User.builder()
                .name("Alice")
                .username("alice")
                .email("alice@example.com")
                .password("hashed")
                .enabled(true)
                .build();
        setId(user, id);
        return user;
    }

    /**
     * Builds an ItemDetails with given id, price, discount, and stock.
     */
    private ItemDetails buildItemDetails(long id, String sku,
                                         BigDecimal price,
                                         BigDecimal discount,
                                         int stock) throws Exception {
        Item item = Item.builder()
                .name("Test Product")
                .description("desc")
                .build();
        setId(item, 99L);

        ItemDetails details = ItemDetails.builder()
                .brand("BrandX")
                .sku(sku)
                .price(price)
                .discountPercentage(discount)
                .stockQuantity(stock)
                .build();
        setId(details, id);
        details.setItem(item);
        return details;
    }

    /**
     * Builds a Cart with given id owned by user.
     */
    private Cart buildCart(long id, User user) throws Exception {
        Cart cart = Cart.builder().user(user).build();
        setId(cart, id);
        return cart;
    }

    /**
     * Builds a CartItem with given id, cart, itemDetails and quantity.
     */
    private CartItem buildCartItem(long id, Cart cart, ItemDetails details, int qty)
            throws Exception {
        CartItem item = CartItem.builder()
                .cart(cart)
                .itemDetails(details)
                .quantity(qty)
                .build();
        setCartItemId(item, id);
        return item;
    }

    // ---------------------------------------------------------------
    // Shared fixtures
    // ---------------------------------------------------------------

    private User alice;
    private ItemDetails stubDetails;
    private Cart aliceCart;

    @BeforeEach
    void setUp() throws Exception {
        alice = buildUser(1L);
        stubDetails = buildItemDetails(10L, "SKU-001",
                new BigDecimal("100.00"), new BigDecimal("10.00"), 50);
        aliceCart = buildCart(1L, alice);
    }

    // ---------------------------------------------------------------
    // create()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Creates a new cart and a new cart item when the user has no cart yet")
        void create_noExistingCart_createsCartAndItem() {
            AddCartItemRequest request = new AddCartItemRequest(10L, 2);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(aliceCart);
            when(itemDetailsRepository.findById(10L)).thenReturn(Optional.of(stubDetails));
            when(cartItemRepository.findByCartAndItemDetails(aliceCart, stubDetails))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
                CartItem ci = inv.getArgument(0);
                try {
                    setCartItemId(ci, 100L);
                } catch (Exception ignored) {
                }
                return ci;
            });

            CartItemResponse response = cartService.create(alice, request);

            assertThat(response).isNotNull();
            assertThat(response.quantity()).isEqualTo(2);
            assertThat(response.sku()).isEqualTo("SKU-001");

            // A new cart must be persisted
            verify(cartRepository).save(any(Cart.class));
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Reuses existing cart when user already has one")
        void create_existingCart_reuseCart() {
            AddCartItemRequest request = new AddCartItemRequest(10L, 1);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(itemDetailsRepository.findById(10L)).thenReturn(Optional.of(stubDetails));
            when(cartItemRepository.findByCartAndItemDetails(aliceCart, stubDetails))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

            cartService.create(alice, request);

            // Must NOT create a new cart
            verify(cartRepository, never()).save(any(Cart.class));
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Merges quantity when the product is already in the cart")
        void create_duplicateProduct_increasesQuantity() throws Exception {
            AddCartItemRequest request = new AddCartItemRequest(10L, 3);
            CartItem existing = buildCartItem(100L, aliceCart, stubDetails, 5);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(itemDetailsRepository.findById(10L)).thenReturn(Optional.of(stubDetails));
            when(cartItemRepository.findByCartAndItemDetails(aliceCart, stubDetails))
                    .thenReturn(Optional.of(existing));
            when(cartItemRepository.save(existing)).thenReturn(existing);

            CartItemResponse response = cartService.create(alice, request);

            // 5 existing + 3 added = 8
            assertThat(response.quantity()).isEqualTo(8);
            verify(cartItemRepository).save(existing);
        }

        @Test
        @DisplayName("Throws InsufficientStockException when requested quantity exceeds stock")
        void create_insufficientStock_throws() {
            // Stock is 50, requesting 51
            AddCartItemRequest request = new AddCartItemRequest(10L, 51);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(itemDetailsRepository.findById(10L)).thenReturn(Optional.of(stubDetails));

            assertThatThrownBy(() -> cartService.create(alice, request))
                    .isInstanceOf(InsufficientStockException.class);

            verify(cartItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws InsufficientStockException when merge would exceed stock")
        void create_mergeExceedsStock_throws() throws Exception {
            // Existing qty=48, adding 5 → total 53 > 50
            CartItem existing = buildCartItem(100L, aliceCart, stubDetails, 48);
            AddCartItemRequest request = new AddCartItemRequest(10L, 5);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(itemDetailsRepository.findById(10L)).thenReturn(Optional.of(stubDetails));
            when(cartItemRepository.findByCartAndItemDetails(aliceCart, stubDetails))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> cartService.create(alice, request))
                    .isInstanceOf(InsufficientStockException.class);

            verify(cartItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws ItemDetailsNotFoundException when item details ID does not exist")
        void create_itemDetailsNotFound_throws() {
            AddCartItemRequest request = new AddCartItemRequest(999L, 1);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(itemDetailsRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.create(alice, request))
                    .isInstanceOf(ItemDetailsNotFoundException.class);

            verify(cartItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Computes correct effective price (price × (1 - discount%))")
        void create_priceCalculation_appliesDiscount() {
            // price=200, discount=25% → effective=150
            AddCartItemRequest request = new AddCartItemRequest(10L, 2);
            ItemDetails discountedDetails;
            try {
                discountedDetails = buildItemDetails(10L, "SKU-001",
                        new BigDecimal("200.00"), new BigDecimal("25.00"), 50);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(itemDetailsRepository.findById(10L)).thenReturn(Optional.of(discountedDetails));
            when(cartItemRepository.findByCartAndItemDetails(aliceCart, discountedDetails))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

            CartItemResponse response = cartService.create(alice, request);

            assertThat(response.unitPrice()).isEqualByComparingTo("150.00");
            assertThat(response.lineTotal()).isEqualByComparingTo("300.00");
        }
    }

    // ---------------------------------------------------------------
    // get()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("get()")
    class Get {

        @Test
        @DisplayName("Returns populated CartResponse when user has a cart with items")
        void get_withItems_returnsResponse() throws Exception {
            CartItem item1 = buildCartItem(1L, aliceCart, stubDetails, 2);
            CartItem item2 = buildCartItem(2L, aliceCart, stubDetails, 3);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(cartItemRepository.findAllByCart(aliceCart)).thenReturn(List.of(item1, item2));

            CartResponse response = cartService.get(alice);

            assertThat(response.cartId()).isEqualTo(1L);
            assertThat(response.totalItems()).isEqualTo(2);
            assertThat(response.items()).hasSize(2);
            // price=100, discount=10% → effective=90; (2+3)*90 = 450
            assertThat(response.subtotal()).isEqualByComparingTo("450.00");
        }

        @Test
        @DisplayName("Returns empty CartResponse when user has no cart")
        void get_noCart_returnsEmptyResponse() {
            when(cartRepository.findByUser(alice)).thenReturn(Optional.empty());

            CartResponse response = cartService.get(alice);

            assertThat(response.cartId()).isNull();
            assertThat(response.totalItems()).isEqualTo(0);
            assertThat(response.items()).isEmpty();
            assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Returns CartResponse with zero subtotal when cart exists but is empty")
        void get_emptyCart_returnsZeroSubtotal() {
            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(cartItemRepository.findAllByCart(aliceCart)).thenReturn(List.of());

            CartResponse response = cartService.get(alice);

            assertThat(response.cartId()).isEqualTo(1L);
            assertThat(response.totalItems()).isEqualTo(0);
            assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ---------------------------------------------------------------
    // getById()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("Returns CartItemResponse when item belongs to the user's cart")
        void getById_owned_returnsResponse() throws Exception {
            CartItem cartItem = buildCartItem(100L, aliceCart, stubDetails, 3);

            when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));
            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));

            CartItemResponse response = cartService.getById(alice, 100L);

            assertThat(response.cartItemId()).isEqualTo(100L);
            assertThat(response.quantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Throws CartItemNotFoundException when cart item ID does not exist")
        void getById_notFound_throws() {
            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.getById(alice, 999L))
                    .isInstanceOf(CartItemNotFoundException.class);
        }

        @Test
        @DisplayName("Throws CartItemAccessDeniedException when item belongs to another user's cart")
        void getById_wrongOwner_throwsAccessDenied() throws Exception {
            User bob = buildUser(2L);
            Cart bobCart = buildCart(2L, bob);
            CartItem bobItem = buildCartItem(200L, bobCart, stubDetails, 1);

            when(cartItemRepository.findById(200L)).thenReturn(Optional.of(bobItem));
            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));

            assertThatThrownBy(() -> cartService.getById(alice, 200L))
                    .isInstanceOf(CartItemAccessDeniedException.class);
        }
    }

    // ---------------------------------------------------------------
    // update()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Updates quantity and returns updated CartItemResponse")
        void update_valid_updatesQuantity() throws Exception {
            CartItem cartItem = buildCartItem(100L, aliceCart, stubDetails, 3);
            UpdateCartItemRequest request = new UpdateCartItemRequest(7);

            when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));
            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

            CartItemResponse response = cartService.update(alice, 100L, request);

            assertThat(response.quantity()).isEqualTo(7);
            assertThat(cartItem.getQuantity()).isEqualTo(7);

            ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
            verify(cartItemRepository).save(captor.capture());
            assertThat(captor.getValue().getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("Throws InsufficientStockException when new quantity exceeds stock")
        void update_exceedsStock_throws() throws Exception {
            CartItem cartItem = buildCartItem(100L, aliceCart, stubDetails, 3);
            // stock=50, requesting 51
            UpdateCartItemRequest request = new UpdateCartItemRequest(51);

            when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));
            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));

            assertThatThrownBy(() -> cartService.update(alice, 100L, request))
                    .isInstanceOf(InsufficientStockException.class);

            verify(cartItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws CartItemNotFoundException when cart item does not exist")
        void update_notFound_throws() {
            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.update(alice, 999L, new UpdateCartItemRequest(2)))
                    .isInstanceOf(CartItemNotFoundException.class);

            verify(cartItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws CartItemAccessDeniedException when item belongs to another user")
        void update_wrongOwner_throwsAccessDenied() throws Exception {
            User bob = buildUser(2L);
            Cart bobCart = buildCart(2L, bob);
            CartItem bobItem = buildCartItem(200L, bobCart, stubDetails, 1);

            when(cartItemRepository.findById(200L)).thenReturn(Optional.of(bobItem));
            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));

            assertThatThrownBy(() -> cartService.update(alice, 200L, new UpdateCartItemRequest(2)))
                    .isInstanceOf(CartItemAccessDeniedException.class);

            verify(cartItemRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------
    // delete()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Deletes the cart item when it belongs to the user's cart")
        void delete_owned_deletesSuccessfully() throws Exception {
            CartItem cartItem = buildCartItem(100L, aliceCart, stubDetails, 2);

            when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));
            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));

            cartService.delete(alice, 100L);

            verify(cartItemRepository).delete(cartItem);
        }

        @Test
        @DisplayName("Throws CartItemNotFoundException when cart item does not exist")
        void delete_notFound_throws() {
            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.delete(alice, 999L))
                    .isInstanceOf(CartItemNotFoundException.class);

            verify(cartItemRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Throws CartItemAccessDeniedException when item belongs to another user's cart")
        void delete_wrongOwner_throwsAccessDenied() throws Exception {
            User bob = buildUser(2L);
            Cart bobCart = buildCart(2L, bob);
            CartItem bobItem = buildCartItem(200L, bobCart, stubDetails, 1);

            when(cartItemRepository.findById(200L)).thenReturn(Optional.of(bobItem));
            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));

            assertThatThrownBy(() -> cartService.delete(alice, 200L))
                    .isInstanceOf(CartItemAccessDeniedException.class);

            verify(cartItemRepository, never()).delete(any());
        }
    }

    // ---------------------------------------------------------------
    // Price calculation edge cases
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Price calculation")
    class PriceCalculation {

        @Test
        @DisplayName("Computes zero discount correctly (unitPrice == originalPrice)")
        void price_zeroDiscount_equalsOriginalPrice() throws Exception {
            ItemDetails noDiscount = buildItemDetails(20L, "SKU-002",
                    new BigDecimal("299.99"), BigDecimal.ZERO, 10);
            AddCartItemRequest request = new AddCartItemRequest(20L, 1);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(itemDetailsRepository.findById(20L)).thenReturn(Optional.of(noDiscount));
            when(cartItemRepository.findByCartAndItemDetails(aliceCart, noDiscount))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CartItemResponse response = cartService.create(alice, request);

            assertThat(response.unitPrice()).isEqualByComparingTo("299.99");
            assertThat(response.lineTotal()).isEqualByComparingTo("299.99");
        }

        @Test
        @DisplayName("Computes 100% discount correctly (unitPrice == 0)")
        void price_fullDiscount_returnsZero() throws Exception {
            ItemDetails fullDiscount = buildItemDetails(30L, "SKU-003",
                    new BigDecimal("100.00"), new BigDecimal("100.00"), 10);
            AddCartItemRequest request = new AddCartItemRequest(30L, 3);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(itemDetailsRepository.findById(30L)).thenReturn(Optional.of(fullDiscount));
            when(cartItemRepository.findByCartAndItemDetails(aliceCart, fullDiscount))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CartItemResponse response = cartService.create(alice, request);

            assertThat(response.unitPrice()).isEqualByComparingTo("0.00");
            assertThat(response.lineTotal()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("Rounds effective price to 2 decimal places using HALF_UP")
        void price_roundingHalfUp_correctScale() throws Exception {
            // price=10, discount=33.33% → effective = 10*(1-0.3333) = 6.667 → rounds to 6.67
            ItemDetails details = buildItemDetails(40L, "SKU-004",
                    new BigDecimal("10.00"), new BigDecimal("33.33"), 10);
            AddCartItemRequest request = new AddCartItemRequest(40L, 1);

            when(cartRepository.findByUser(alice)).thenReturn(Optional.of(aliceCart));
            when(itemDetailsRepository.findById(40L)).thenReturn(Optional.of(details));
            when(cartItemRepository.findByCartAndItemDetails(aliceCart, details))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CartItemResponse response = cartService.create(alice, request);

            assertThat(response.unitPrice().scale()).isEqualTo(2);
            assertThat(response.unitPrice()).isEqualByComparingTo("6.67");
        }
    }
}