package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.cart.CartItemResponse;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.review.ItemReviewResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.dto.user.UserRequest;
import com.pkmprojects.shoppiq.dto.user.UserResponse;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.cart.Cart;
import com.pkmprojects.shoppiq.entity.cart.CartItem;
import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.order.OrderAddressSnapshot;
import com.pkmprojects.shoppiq.entity.order.OrderItem;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.exception.business.SlugGenerationFailedException;
import com.pkmprojects.shoppiq.exception.general.address.AddressNotFoundException;
import com.pkmprojects.shoppiq.exception.general.cart.CartEmptyException;
import com.pkmprojects.shoppiq.exception.general.inventory.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.general.user.UserNotFoundException;
import com.pkmprojects.shoppiq.repository.address.AddressRepository;
import com.pkmprojects.shoppiq.repository.cart.CartItemRepository;
import com.pkmprojects.shoppiq.repository.cart.CartRepository;
import com.pkmprojects.shoppiq.repository.item.ItemReviewRepository;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import com.pkmprojects.shoppiq.service.cart.CartServiceImpl;
import com.pkmprojects.shoppiq.service.category.CategoryLookupService;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.item.ItemWriteService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsLookupService;
import com.pkmprojects.shoppiq.service.role.RoleService;
import com.pkmprojects.shoppiq.service.seller.SellerLookupService;
import com.pkmprojects.shoppiq.service.seller.SellerWriteService;
import com.pkmprojects.shoppiq.util.PriceUtil;
import com.pkmprojects.shoppiq.util.SlugUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link AdminTestDataService} containing bulk-creation
 * business logic for populating test data.
 *
 * <p>Creates users, items, addresses, reviews, sellers, cart items, and orders
 * in bulk within single transactions. Used by {@code AdminTestDataController}
 * for development and testing environments. All mutations are transactional
 * to ensure atomicity across the multiple entity types being created.</p>
 *
 * <p>Delegates to {@code RoleService} for role resolution, {@code CartServiceImpl}
 * for cart operations, and {@code ItemWriteService}/{@code SellerWriteService}
 * for persistence. Password encoding uses the injected {@code PasswordEncoder}
 * for consistent BCrypt hashing.</p>
 *
 * @author prabhatkrmishra
 * @see AdminTestDataService
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminTestDataServiceImpl implements AdminTestDataService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ItemLookupService itemLookupService;
    private final ItemWriteService itemWriteService;
    private final ItemReviewRepository itemReviewRepository;
    private final SellerLookupService sellerLookupService;
    private final SellerWriteService sellerWriteService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemDetailsLookupService itemDetailsLookupService;
    private final CategoryLookupService categoryLookupService;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final Clock clock;

    public AdminTestDataServiceImpl(
            UserRepository userRepository,
            AddressRepository addressRepository,
            ItemLookupService itemLookupService,
            ItemWriteService itemWriteService,
            ItemReviewRepository itemReviewRepository,
            SellerLookupService sellerLookupService,
            SellerWriteService sellerWriteService,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ItemDetailsLookupService itemDetailsLookupService,
            CategoryLookupService categoryLookupService,
            OrderRepository orderRepository,
            PasswordEncoder passwordEncoder,
            RoleService roleService,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.itemLookupService = itemLookupService;
        this.itemWriteService = itemWriteService;
        this.itemReviewRepository = itemReviewRepository;
        this.sellerLookupService = sellerLookupService;
        this.sellerWriteService = sellerWriteService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.itemDetailsLookupService = itemDetailsLookupService;
        this.categoryLookupService = categoryLookupService;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.clock = clock;
    }

    /**
     * Creates multiple user accounts with encoded passwords and default CUSTOMER role.
     *
     * @param request bulk user creation payload
     * @return list of created user responses
     */
    @Override
    public List<UserResponse> createBulkUsers(BulkUserRequest request) {
        List<UserResponse> responses = new ArrayList<>();

        for (UserRequest userReq : request.users()) {
            User user = User.builder()
                    .name(userReq.getName())
                    .email(userReq.getEmail())
                    .username(userReq.getUsername())
                    .password(passwordEncoder.encode(userReq.getPassword()))
                    .roles(Set.of(roleService.getCustomerRole()))
                    .build();

            User saved = userRepository.save(user);
            responses.add(UserResponse.fromEntity(saved));
        }

        return responses;
    }

    /**
     * Creates multiple items with auto-approval, category/seller validation, and unique slug generation.
     *
     * @param request bulk item creation payload
     * @return list of created item responses
     */
    @Override
    public List<ItemResponse> createBulkItems(BulkAdminItemRequest request) {
        List<ItemResponse> responses = new ArrayList<>();

        for (AdminItemRequest itemReq : request.items()) {
            Category category = categoryLookupService.findById(itemReq.categoryId())
                    .orElseThrow(() -> new RuntimeException(
                            "Category with id '%d' was not found.".formatted(itemReq.categoryId())));

            Seller seller = sellerLookupService.findById(itemReq.sellerId())
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
                    .slug(generateUniqueSlug(itemReq.name()))
                    .description(itemReq.description())
                    .seller(seller)
                    .publishingStatus(ProductPublishingStatus.PUBLISHED)
                    .itemDetails(itemDetails)
                    .build();

            itemDetails.setItem(item);
            Item saved = itemWriteService.save(item);
            responses.add(ItemResponse.fromEntity(saved));
        }

        return responses;
    }

    /**
     * Creates multiple addresses respecting the one-default invariant per user.
     *
     * @param request bulk address creation payload
     * @return list of created address responses
     */
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

    /**
     * Creates multiple product reviews, preventing duplicates per user-item pair.
     *
     * @param request bulk review creation payload
     * @return list of created review responses
     */
    @Override
    public List<ItemReviewResponse> createBulkReviews(BulkReviewRequest request) {
        List<ItemReviewResponse> responses = new ArrayList<>();

        for (AdminReviewItem item : request.reviews()) {
            User user = findUser(item.userId());

            Item itemEntity = itemLookupService.findById(item.itemId())
                    .orElseThrow(() -> com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException.id(item.itemId()));

            if (itemReviewRepository.existsByUserIdAndItemId(item.userId(), item.itemId())) {
                throw com.pkmprojects.shoppiq.exception.general.item.DuplicateItemReviewException.userId(item.userId());
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

    /**
     * Creates multiple seller profiles, preventing duplicates per user.
     *
     * @param request bulk seller creation payload
     * @return list of created seller responses
     */
    @Override
    public List<SellerResponse> createBulkSellers(BulkSellerRequest request) {
        List<SellerResponse> responses = new ArrayList<>();

        for (AdminSellerItem item : request.sellers()) {
            User user = findUser(item.userId());

            if (sellerLookupService.existsByUserId(item.userId())) {
                throw com.pkmprojects.shoppiq.exception.general.seller.SellerAlreadyExistsException.forUser(item.userId());
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
                    .joinedAt(Instant.now(clock))
                    .build();

            Seller saved = sellerWriteService.save(seller);
            responses.add(SellerResponse.fromEntity(saved));
        }

        return responses;
    }

    /**
     * Adds items to user carts in bulk, merging quantities for existing duplicates.
     *
     * @param request bulk cart item creation payload
     * @return list of created cart item responses
     */
    @Override
    public List<CartItemResponse> createBulkCartItems(BulkCartRequest request) {
        List<CartItemResponse> responses = new ArrayList<>();

        for (AdminCartItem item : request.cartItems()) {
            User user = findUser(item.userId());

            Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() -> cartRepository.save(
                            Cart.builder().user(user).build()));

            ItemDetails itemDetails = itemDetailsLookupService
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

    /**
     * Creates multiple orders via full checkout flow — validates stock, reduces inventory, clears carts.
     *
     * @param request bulk order creation payload
     * @return list of checkout responses
     */
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

            BigDecimal deliveryCharge = BigDecimal.ZERO;
            BigDecimal codSurcharge = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal grandTotal = subtotal.add(deliveryCharge).add(codSurcharge).add(tax).subtract(discount);

            Order order = Order.builder()
                    .user(user)
                    .address(address)
                    .shippingAddress(OrderAddressSnapshot.from(address))
                    .status(OrderStatus.PLACED)
                    .paymentMethod(item.paymentMethod())
                    .paymentStatus(PaymentStatus.PENDING)
                    .subtotal(subtotal)
                    .deliveryCharge(deliveryCharge)
                    .codSurcharge(codSurcharge)
                    .tax(tax)
                    .discount(discount)
                    .grandTotal(grandTotal)
                    .placedAt(Instant.now(clock))
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
                    order.getId(), order.getStatus(), order.getSubtotal(),
                    order.getDiscount(), order.getDeliveryCharge(), order.getCodSurcharge(),
                    order.getGrandTotal(),
                    order.getDeliveryType(), null, null));
        }

        return responses;
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /**
     * Finds a user by ID or throws {@link UserNotFoundException}.
     */
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.id(userId));
    }

    /**
     * Maps a {@link CartItem} to a {@link CartItemResponse}.
     *
     * <p>Delegates pricing calculation to {@link PriceUtil#effectivePrice}
     * to avoid duplicating business logic from {@link CartServiceImpl}.</p>
     */
    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        ItemDetails details = cartItem.getItemDetails();
        java.math.BigDecimal unitPrice = PriceUtil.effectivePrice(details);
        java.math.BigDecimal lineTotal = unitPrice.multiply(
                java.math.BigDecimal.valueOf(cartItem.getQuantity()));

        String itemName = details.getItem() != null ? details.getItem().getName() : "";
        Long itemId = details.getItem() != null ? details.getItem().getId() : null;
        String itemSlug = details.getItem() != null ? details.getItem().getSlug() : "";

        return new CartItemResponse(
                cartItem.getId(),
                details.getId(),
                itemId,
                itemSlug,
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
     * Generates a unique URL-friendly slug.
     *
     * @param itemName item name
     * @return unique slug
     */
    private String generateUniqueSlug(String itemName) {
        String baseSlug = SlugUtil.toSlug(itemName);
        String slug = baseSlug;
        int counter = 2;

        while (itemLookupService.existsBySlug(slug)) {
            if (counter > 1000) {
                throw SlugGenerationFailedException.forEntity("item", itemName, 1000);
            }
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}
