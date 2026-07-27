package com.pkmprojects.shoppiq.service.checkout;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutRequest;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.order.OrderCalculationRequest;
import com.pkmprojects.shoppiq.dto.order.OrderCalculationResponse;
import com.pkmprojects.shoppiq.dto.order.OrderResponse;
import com.pkmprojects.shoppiq.dto.promo.CartItemPreview;
import com.pkmprojects.shoppiq.entity.address.Address;
import com.pkmprojects.shoppiq.entity.cart.Cart;
import com.pkmprojects.shoppiq.entity.cart.CartItem;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.order.OrderAddressSnapshot;
import com.pkmprojects.shoppiq.entity.order.OrderItem;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.events.OrderPlacedEvent;
import com.pkmprojects.shoppiq.exception.general.address.AddressAccessDeniedException;
import com.pkmprojects.shoppiq.exception.general.address.AddressNotFoundException;
import com.pkmprojects.shoppiq.exception.general.cart.CartEmptyException;
import com.pkmprojects.shoppiq.exception.general.inventory.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.general.inventory.StockConflictException;
import com.pkmprojects.shoppiq.exception.general.order.OrderAccessDeniedException;
import com.pkmprojects.shoppiq.exception.general.order.OrderCannotBeCancelledException;
import com.pkmprojects.shoppiq.exception.general.order.OrderInvalidStatusTransitionException;
import com.pkmprojects.shoppiq.exception.general.order.OrderNotFoundException;
import com.pkmprojects.shoppiq.repository.address.AddressRepository;
import com.pkmprojects.shoppiq.repository.cart.CartRepository;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.service.cart.CartService;
import com.pkmprojects.shoppiq.service.inventory.InventoryService;
import com.pkmprojects.shoppiq.service.payment.PaymentService;
import com.pkmprojects.shoppiq.service.promo.PromoCodeService;
import com.pkmprojects.shoppiq.util.PriceUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

/**
 * Handles the full checkout workflow inside a single database transaction.
 *
 * <p>Checkout steps:</p>
 * <ol>
 *   <li>Load cart — throw {@link CartEmptyException} if missing or empty.</li>
 *   <li>Load address — validate ownership.</li>
 *   <li>Validate stock for every cart item.</li>
 *   <li>Calculate totals (subtotal, shipping, tax, discount, grandTotal).</li>
 *   <li>Persist {@link Order} with {@code PLACED} status.</li>
 *   <li>Persist {@link OrderItem} snapshots.</li>
 *   <li>Reduce inventory via {@link InventoryService}.</li>
 *   <li>Clear the cart via {@link CartService}.</li>
 *   <li>Create payment via {@link PaymentService}.</li>
 *   <li>Publish {@link OrderPlacedEvent} for async side effects.</li>
 * </ol>
 *
 * <p>Post-order side effects (email, promo usage recording) are handled
 * asynchronously by {@link com.pkmprojects.shoppiq.events.OrderPlacedEventListener}
 * after the checkout transaction commits.</p>
 *
 * <p>Shipping charges:</p>
 * <ul>
 *   <li>{@link DeliveryType#NORMAL} — free shipping</li>
 *   <li>{@link DeliveryType#EXPRESS_1DAY} — $7.50 express delivery charge</li>
 *   <li>{@link PaymentMethod#COD} — $5.00 cash-on-delivery surcharge</li>
 * </ul>
 *
 * <h2>Dependency Graph</h2>
 * <pre>
 * CheckoutServiceImpl
 *   ├── CartRepository          (load cart)
 *   ├── AddressRepository       (load + validate address)
 *   ├── OrderRepository         (persist order)
 *   ├── CartService             (clear cart after order)
 *   ├── InventoryService        (reduce stock after order)
 *   ├── PaymentService          (create payment record)
 *   ├── PromoCodeService        (validate promo, calculate discount)
 *   ├── ApplicationEventPublisher (dispatch OrderPlacedEvent)
 *   └── Clock                   (deterministic time for tests)
 * </pre>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class CheckoutServiceImpl {

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final PromoCodeService promoCodeService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public CheckoutServiceImpl(CartRepository cartRepository,
                               AddressRepository addressRepository,
                               OrderRepository orderRepository,
                               CartService cartService,
                               InventoryService inventoryService,
                               PaymentService paymentService,
                               PromoCodeService promoCodeService,
                               ApplicationEventPublisher eventPublisher,
                               Clock clock) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.promoCodeService = promoCodeService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    // =========================================================
    // Checkout
    // =========================================================

    /**
     * Executes the full checkout flow for an authenticated user.
     *
     * @param user    authenticated customer
     * @param request checkout payload
     * @return lightweight checkout response containing orderId and grandTotal
     * @throws StockConflictException if inventory was modified concurrently
     */
    public CheckoutResponse checkout(User user, CheckoutRequest request) {
        try {
            return doCheckout(user, request);
        } catch (OptimisticLockingFailureException e) {
            throw StockConflictException.forOptimisticLock(
                    "Inventory was modified by another customer. Please refresh and try again.");
        }
    }

    private CheckoutResponse doCheckout(User user, CheckoutRequest request) {

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(CartEmptyException::new);

        List<CartItem> cartItems = cart.getItems();

        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartEmptyException();
        }

        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> AddressNotFoundException.id(request.addressId()));

        if (address.getUser() == null || !address.getUser().getId().equals(user.getId())) {
            throw AddressAccessDeniedException.forAddress(request.addressId());
        }

        cartItems.forEach(cartItem -> {
            ItemDetails details = cartItem.getItemDetails();
            int available = details.getStockQuantity();
            int requested = cartItem.getQuantity();
            if (available < requested) {
                throw InsufficientStockException.forItem(
                        details.getSku(), requested, available);
            }
        });

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            BigDecimal lineTotal = PriceUtil.effectivePrice(cartItem.getItemDetails())
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        PromoCode appliedPromoCode = null;

        // Delivery charge based on delivery type
        DeliveryType deliveryType = request.deliveryType() != null
                ? request.deliveryType() : DeliveryType.NORMAL;
        BigDecimal deliveryCharge = BigDecimal.ZERO;
        if (deliveryType == DeliveryType.EXPRESS_1DAY) {
            deliveryCharge = new BigDecimal("7.50");
        }

        // COD surcharge
        BigDecimal codSurcharge = BigDecimal.ZERO;
        if (request.paymentMethod() == PaymentMethod.COD) {
            codSurcharge = new BigDecimal("5.00");
        }

        List<CartItemPreview> cartPreviews = cartItems.stream()
                .map(ci -> new CartItemPreview(
                        ci.getItemDetails().getId(),
                        ci.getQuantity(),
                        PriceUtil.effectivePrice(ci.getItemDetails())))
                .toList();

        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            appliedPromoCode = promoCodeService.validateAndCalculate(
                    request.promoCode(), user, subtotal, cartPreviews);
            discount = promoCodeService.calculateDiscount(appliedPromoCode, subtotal, cartPreviews);
        }

        BigDecimal grandTotal = subtotal.add(deliveryCharge).add(codSurcharge).add(tax).subtract(discount);

        Order order = Order.builder()
                .user(user)
                .address(address)
                .shippingAddress(OrderAddressSnapshot.from(address))
                .status(OrderStatus.PLACED)
                .paymentMethod(request.paymentMethod())
                .deliveryType(deliveryType)
                .paymentStatus(PaymentStatus.PENDING)
                .subtotal(subtotal)
                .deliveryCharge(deliveryCharge)
                .codSurcharge(codSurcharge)
                .tax(tax)
                .discount(discount)
                .grandTotal(grandTotal)
                .promoCode(appliedPromoCode)
                .promoCodeSnapshot(appliedPromoCode != null ? appliedPromoCode.getCode() : null)
                .placedAt(clock.instant())
                .build();

        orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            ItemDetails details = cartItem.getItemDetails();
            Item item = details.getItem();

            BigDecimal lineSubtotal = PriceUtil.effectivePrice(details)
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .itemDetails(details)
                    .itemNameSnapshot(item != null ? item.getName() : details.getItem().getName())
                    .unitPriceSnapshot(details.getPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(lineSubtotal)
                    .build();

            order.addOrderItem(orderItem);
        }

        inventoryService.reduceStock(cartItems);
        cartService.clearCart(user);

        Payment payment = paymentService.createPayment(order);

        eventPublisher.publishEvent(new OrderPlacedEvent(order, user, appliedPromoCode));

        return CheckoutResponse.from(order, payment.getId());
    }

    // =========================================================
    // Cost preview (no persistence)
    // =========================================================

    /**
     * Calculates the full order cost breakdown from the user's current cart
     * without placing an order.
     *
     * <p>Use this on the payment page so every cost component — delivery charge,
     * COD surcharge, discount, grand total — is server-authoritative.</p>
     *
     * @param user    authenticated customer
     * @param request payment and delivery selections
     * @return full cost breakdown
     * @throws CartEmptyException if the cart is missing or empty
     */
    @Transactional(readOnly = true)
    public OrderCalculationResponse calculateOrderSummary(User user, OrderCalculationRequest request) {

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(CartEmptyException::new);

        List<CartItem> cartItems = cart.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartEmptyException();
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            BigDecimal lineTotal = PriceUtil.effectivePrice(cartItem.getItemDetails())
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }

        DeliveryType deliveryType = request.deliveryType() != null
                ? request.deliveryType() : DeliveryType.NORMAL;

        BigDecimal deliveryCharge = BigDecimal.ZERO;
        if (deliveryType == DeliveryType.EXPRESS_1DAY) {
            deliveryCharge = new BigDecimal("7.50");
        }

        BigDecimal codSurcharge = BigDecimal.ZERO;
        if (request.paymentMethod() == PaymentMethod.COD) {
            codSurcharge = new BigDecimal("5.00");
        }

        BigDecimal tax = BigDecimal.ZERO;

        List<CartItemPreview> cartPreviews = cartItems.stream()
                .map(ci -> new CartItemPreview(
                        ci.getItemDetails().getId(),
                        ci.getQuantity(),
                        PriceUtil.effectivePrice(ci.getItemDetails())))
                .toList();

        BigDecimal discount = BigDecimal.ZERO;
        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            PromoCode appliedPromoCode = promoCodeService.validateAndCalculate(
                    request.promoCode(), user, subtotal, cartPreviews);
            discount = promoCodeService.calculateDiscount(appliedPromoCode, subtotal, cartPreviews);
        }

        BigDecimal grandTotal = subtotal.add(deliveryCharge).add(codSurcharge)
                .add(tax).subtract(discount);

        return new OrderCalculationResponse(
                subtotal,
                deliveryCharge,
                codSurcharge,
                discount,
                grandTotal
        );
    }

    // =========================================================
    // Query
    // =========================================================

    /**
     * Returns all orders belonging to the authenticated user.
     *
     * @param user authenticated customer
     * @return list of full order responses, newest first
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(User user) {
        return orderRepository.findAllByUserOrderByPlacedAtDesc(user)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "placedAt"));
        var orderPage = orderRepository.findAllByUserOrderByPlacedAtDesc(user, pageable);
        return PageResponse.of(orderPage, OrderResponse::from);
    }

    /**
     * Returns a single order that must belong to the authenticated user.
     *
     * @param user    authenticated customer
     * @param orderId target order id
     * @return full order response
     */
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(User user, Long orderId) {
        Order order = findOrderOrThrow(orderId);
        if (!isAdmin(user)) {
            assertOwnership(user, order);
        }
        return OrderResponse.from(order);
    }

    // =========================================================
    // Cancellation
    // =========================================================

    /**
     * Requests cancellation for an order in {@code PLACED} status.
     *
     * <p>Sets the order status to {@code CANCEL_REQUEST} for admin/seller approval.</p>
     *
     * @param user    authenticated customer
     * @param orderId target order id
     */
    public void cancelOrder(User user, Long orderId) {
        Order order = findOrderOrThrow(orderId);
        assertOwnership(user, order);

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new OrderCannotBeCancelledException(orderId, order.getStatus());
        }

        order.setStatus(OrderStatus.CANCEL_REQUEST);
        orderRepository.save(order);
    }

    // =========================================================
    // Return
    // =========================================================

    /**
     * Requests a return for an order in {@code DELIVERED} status.
     *
     * <p>Sets the order status to {@code RETURN_REQUEST} for admin/seller processing.</p>
     *
     * @param user    authenticated customer
     * @param orderId target order id
     */
    public void requestReturn(User user, Long orderId) {
        Order order = findOrderOrThrow(orderId);
        assertOwnership(user, order);

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new OrderInvalidStatusTransitionException(order.getStatus(), OrderStatus.RETURN_REQUEST);
        }

        order.setStatus(OrderStatus.RETURN_REQUEST);
        orderRepository.save(order);
    }

    // =========================================================
    // Refund
    // =========================================================

    /**
     * Requests a refund for an order in {@code DELIVERED} status.
     *
     * <p>Sets the order status to {@code REFUND_REQUEST} for admin/seller processing.</p>
     *
     * @param user    authenticated customer
     * @param orderId target order id
     */
    public void requestRefund(User user, Long orderId) {
        Order order = findOrderOrThrow(orderId);
        assertOwnership(user, order);

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new OrderInvalidStatusTransitionException(order.getStatus(), OrderStatus.REFUND_REQUEST);
        }

        order.setStatus(OrderStatus.REFUND_REQUEST);
        orderRepository.save(order);
    }

    // =========================================================
    // Replacement
    // =========================================================

    /**
     * Requests a replacement for an order in {@code DELIVERED} status.
     *
     * <p>Sets the order status to {@code REPLACE_REQUEST} for admin/seller processing.</p>
     *
     * @param user    authenticated customer
     * @param orderId target order id
     */
    public void requestReplacement(User user, Long orderId) {
        Order order = findOrderOrThrow(orderId);
        assertOwnership(user, order);

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new OrderInvalidStatusTransitionException(order.getStatus(), OrderStatus.REPLACE_REQUEST);
        }

        order.setStatus(OrderStatus.REPLACE_REQUEST);
        orderRepository.save(order);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id '%d' was not found.".formatted(orderId)));
    }

    private void assertOwnership(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw OrderAccessDeniedException.forOrder(order.getId());
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleName()));
    }
}
