package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.request.*;
import com.pkmprojects.shoppiq.dto.response.CartItemResponse;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.dto.response.ItemReviewResponse;
import com.pkmprojects.shoppiq.dto.response.UserResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.dto.user.UserRequest;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.enums.ReviewStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.admin.AdminTestDataService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link AdminTestDataService}.
 *
 * <p>
 * Provides transactional bulk-creation methods for populating test data.
 * All methods run inside a single transaction to maintain referential
 * integrity.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create users with encoded passwords and default CUSTOMER role.</li>
 *     <li>Create addresses for existing users.</li>
 *     <li>Create product reviews for existing users and items.</li>
 *     <li>Create seller profiles for existing users.</li>
 *     <li>Add items to user carts.</li>
 *     <li>Create orders from user carts via checkout flow.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>All write operations run inside transactions.</li>
 *     <li>User context is supplied inline per item (userId).</li>
 *     <li>Duplicates and missing references cause the transaction to roll back.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminTestDataServiceImpl implements AdminTestDataService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ItemRepository itemRepository;
    private final ItemReviewRepository itemReviewRepository;
    private final SellerRepository sellerRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemDetailsRepository itemDetailsRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolesService rolesService;

    public AdminTestDataServiceImpl(
            UserRepository userRepository,
            AddressRepository addressRepository,
            ItemRepository itemRepository,
            ItemReviewRepository itemReviewRepository,
            SellerRepository sellerRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ItemDetailsRepository itemDetailsRepository,
            CategoryRepository categoryRepository,
            OrderRepository orderRepository,
            PasswordEncoder passwordEncoder,
            RolesService rolesService
    ) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.itemRepository = itemRepository;
        this.itemReviewRepository = itemReviewRepository;
        this.sellerRepository = sellerRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.itemDetailsRepository = itemDetailsRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolesService = rolesService;
    }

    @Override
    public List<UserResponse> createBulkUsers(BulkUserRequest request) {
        List<UserResponse> responses = new ArrayList<>();

        for (UserRequest userReq : request.users()) {
            User user = User.builder()
                    .name(userReq.getName())
                    .email(userReq.getEmail())
                    .username(userReq.getUsername())
                    .password(passwordEncoder.encode(userReq.getPassword()))
                    .roles(Set.of(rolesService.getCustomerRole()))
                    .build();

            User saved = userRepository.save(user);
            responses.add(UserResponse.fromEntity(saved));
        }

        return responses;
    }

    @Override
    public List<ItemResponse> createBulkItems(BulkAdminItemRequest request) {
        List<ItemResponse> responses = new ArrayList<>();

        for (AdminItemRequest itemReq : request.items()) {
            Category category = categoryRepository.findById(itemReq.categoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Category with id '%d' was not found.".formatted(itemReq.categoryId())));

            Seller seller = sellerRepository.findById(itemReq.sellerId())
                    .orElseThrow(() -> new RuntimeException(
                            "Seller with id '%d' was not found.".formatted(itemReq.sellerId())));

            ItemDetails itemDetails = ItemDetails.builder()
                    .brand(itemReq.brand())
                    .sku(itemReq.sku())
                    .price(itemReq.price())
                    .stockQuantity(itemReq.stockQuantity())
                    .discountPercentage(itemReq.discountPercentage())
                    .imageUrl(itemReq.imageUrl())
                    .category(category)
                    .build();

            Item item = Item.builder()
                    .name(itemReq.name())
                    .description(itemReq.description())
                    .seller(seller)
                    .publishingStatus(ProductPublishingStatus.PUBLISHED)
                    .itemDetails(itemDetails)
                    .build();

            itemDetails.setItem(item);
            Item saved = itemRepository.save(item);
            responses.add(ItemResponse.fromEntity(saved));
        }

        return responses;
    }

    @Override
    public List<AddressResponse> createBulkAddresses(BulkAddressRequest request) {
        List<AddressResponse> responses = new ArrayList<>();

        for (AdminAddressItem item : request.addresses()) {
            User user = findUser(item.userId());

            if (item.address().isDefault()) {
                addressRepository.clearDefaultForUser(user);
            }

            Address address = Address.builder()
                    .user(user)
                    .label(item.address().label())
                    .fullName(item.address().fullName())
                    .phone(item.address().phone())
                    .line1(item.address().line1())
                    .line2(item.address().line2())
                    .city(item.address().city())
                    .state(item.address().state())
                    .postalCode(item.address().postalCode())
                    .country(item.address().country())
                    .isDefault(item.address().isDefault())
                    .build();

            Address saved = addressRepository.save(address);
            responses.add(AddressResponse.from(saved));
        }

        return responses;
    }

    @Override
    public List<ItemReviewResponse> createBulkReviews(BulkReviewRequest request) {
        List<ItemReviewResponse> responses = new ArrayList<>();

        for (AdminReviewItem item : request.reviews()) {
            User user = findUser(item.userId());

            Item itemEntity = itemRepository.findById(item.itemId())
                    .orElseThrow(() -> new RuntimeException(
                            "Item with id '%d' was not found.".formatted(item.itemId())));

            if (itemReviewRepository.existsByUserIdAndItemId(item.userId(), item.itemId())) {
                throw new RuntimeException(
                        "User '%d' has already reviewed item '%d'."
                                .formatted(item.userId(), item.itemId()));
            }

            ItemReview review = ItemReview.builder()
                    .rating(item.rating())
                    .review(item.review())
                    .status(ReviewStatus.APPROVED)
                    .item(itemEntity)
                    .user(user)
                    .build();

            ItemReview saved = itemReviewRepository.save(review);
            responses.add(ItemReviewResponse.fromEntity(saved));
        }

        return responses;
    }

    @Override
    public List<SellerResponse> createBulkSellers(BulkSellerRequest request) {
        List<SellerResponse> responses = new ArrayList<>();

        for (AdminSellerItem item : request.sellers()) {
            User user = findUser(item.userId());

            if (sellerRepository.existsByUserId(item.userId())) {
                throw new RuntimeException(
                        "User '%d' already has a seller profile."
                                .formatted(item.userId()));
            }

            Seller seller = Seller.builder()
                    .user(user)
                    .businessName(item.seller().businessName())
                    .businessEmail(item.seller().businessEmail())
                    .phone(item.seller().phone())
                    .gstNumber(item.seller().gstNumber())
                    .panNumber(item.seller().panNumber())
                    .verificationStatus(VerificationStatus.PENDING)
                    .sellerStatus(SellerStatus.INACTIVE)
                    .joinedAt(LocalDateTime.now())
                    .build();

            Seller saved = sellerRepository.save(seller);
            responses.add(SellerResponse.fromEntity(saved));
        }

        return responses;
    }

    @Override
    public List<CartItemResponse> createBulkCartItems(BulkCartRequest request) {
        List<CartItemResponse> responses = new ArrayList<>();

        for (AdminCartItem item : request.cartItems()) {
            User user = findUser(item.userId());

            Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() -> cartRepository.save(
                            Cart.builder().user(user).build()));

            ItemDetails itemDetails = itemDetailsRepository
                    .findById(item.itemDetailsId())
                    .orElseThrow(() -> new RuntimeException(
                            "Item details with id '%d' were not found."
                                    .formatted(item.itemDetailsId())));

            CartItem cartItem = cartItemRepository
                    .findByCartAndItemDetails(cart, itemDetails)
                    .map(existing -> {
                        existing.setQuantity(existing.getQuantity() + item.quantity());
                        return existing;
                    })
                    .orElseGet(() -> {
                        CartItem newItem = CartItem.builder()
                                .cart(cart)
                                .itemDetails(itemDetails)
                                .quantity(item.quantity())
                                .build();
                        cart.addItem(newItem);
                        return newItem;
                    });

            CartItem saved = cartItemRepository.save(cartItem);
            responses.add(toCartItemResponse(saved));
        }

        return responses;
    }

    @Override
    public List<CheckoutResponse> createBulkOrders(BulkOrderRequest request) {
        List<CheckoutResponse> responses = new ArrayList<>();

        for (AdminOrderItem item : request.orders()) {
            User user = findUser(item.userId());

            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(CartEmptyException::new);

            List<CartItem> cartItems = cart.getItems();
            if (cartItems == null || cartItems.isEmpty()) {
                throw new CartEmptyException();
            }

            Address address = addressRepository.findById(item.addressId())
                    .orElseThrow(() -> AddressNotFoundException.id(item.addressId()));

            for (CartItem cartItem : cartItems) {
                ItemDetails details = cartItem.getItemDetails();
                int available = details.getStockQuantity();
                int requested = cartItem.getQuantity();
                if (available < requested) {
                    throw InsufficientStockException.forItem(
                            details.getSku(), requested, available);
                }
            }

            BigDecimal subtotal = BigDecimal.ZERO;
            for (CartItem cartItem : cartItems) {
                BigDecimal lineTotal = cartItem.getItemDetails().getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                subtotal = subtotal.add(lineTotal);
            }

            BigDecimal shippingFee = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal grandTotal = subtotal.add(shippingFee).add(tax).subtract(discount);

            Order order = Order.builder()
                    .user(user)
                    .address(address)
                    .status(OrderStatus.PLACED)
                    .paymentMethod(item.paymentMethod())
                    .paymentStatus(PaymentStatus.PENDING)
                    .subtotal(subtotal)
                    .shippingFee(shippingFee)
                    .tax(tax)
                    .discount(discount)
                    .grandTotal(grandTotal)
                    .placedAt(Instant.now())
                    .build();

            orderRepository.save(order);

            for (CartItem cartItem : cartItems) {
                ItemDetails details = cartItem.getItemDetails();
                Item it = details.getItem();

                BigDecimal lineSubtotal = details.getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .itemDetails(details)
                        .itemNameSnapshot(it.getName())
                        .unitPriceSnapshot(details.getPrice())
                        .quantity(cartItem.getQuantity())
                        .subtotal(lineSubtotal)
                        .build();

                order.addOrderItem(orderItem);

                details.setStockQuantity(details.getStockQuantity() - cartItem.getQuantity());
            }

            cart.getItems().clear();
            cartRepository.save(cart);

            responses.add(new CheckoutResponse(
                    order.getId(), order.getStatus(), order.getGrandTotal(), null));
        }

        return responses;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.id(userId));
    }

    /**
     * Maps a {@link CartItem} to a {@link CartItemResponse}.
     *
     * <p>Replicates the mapping logic from {@link CartServiceImpl}.</p>
     */
    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        ItemDetails details = cartItem.getItemDetails();
        java.math.BigDecimal unitPrice = effectivePrice(details);
        java.math.BigDecimal lineTotal = unitPrice.multiply(
                java.math.BigDecimal.valueOf(cartItem.getQuantity()));

        String itemName = details.getItem() != null ? details.getItem().getName() : "";

        return new CartItemResponse(
                cartItem.getId(),
                details.getId(),
                itemName,
                details.getBrand(),
                details.getSku(),
                unitPrice,
                details.getPrice(),
                details.getDiscountPercentage(),
                cartItem.getQuantity(),
                lineTotal,
                details.getImageUrl()
        );
    }

    /**
     * Computes the effective (post-discount) price for an item.
     *
     * <p>Replicates the pricing logic from {@link CartServiceImpl}.</p>
     */
    private java.math.BigDecimal effectivePrice(ItemDetails itemDetails) {
        java.math.BigDecimal discount = itemDetails.getDiscountPercentage()
                .divide(java.math.BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
        return itemDetails.getPrice()
                .multiply(java.math.BigDecimal.ONE.subtract(discount))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
