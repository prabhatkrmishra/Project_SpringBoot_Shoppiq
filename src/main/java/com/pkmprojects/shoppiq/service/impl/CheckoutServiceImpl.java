package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutRequest;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.order.OrderCalculationRequest;
import com.pkmprojects.shoppiq.dto.order.OrderCalculationResponse;
import com.pkmprojects.shoppiq.dto.order.OrderResponse;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.StockConflictException;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.OrderEmailService;
import com.pkmprojects.shoppiq.service.PaymentService;
import com.pkmprojects.shoppiq.service.PromoCodeService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Iterator;
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
 *   <li>Reduce inventory.</li>
 *   <li>Clear the cart.</li>
 * </ol>
 *
 * <p>Shipping charges:</p>
 * <ul>
 *   <li>{@link DeliveryType#NORMAL} — free shipping</li>
 *   <li>{@link DeliveryType#EXPRESS_1DAY} — $7.50 express delivery charge</li>
 *   <li>{@link PaymentMethod#COD} — $5.00 cash-on-delivery surcharge</li>
 * </ul>
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
    private final ItemDetailsRepository itemDetailsRepository;
    private final PaymentService paymentService;
    private final PromoCodeService promoCodeService;
    private final OrderEmailService orderEmailService;
    private final Clock clock;

    public CheckoutServiceImpl(CartRepository cartRepository,
                               AddressRepository addressRepository,
                               OrderRepository orderRepository,
                               ItemDetailsRepository itemDetailsRepository,
                               PaymentService paymentService,
                               PromoCodeService promoCodeService,
                               OrderEmailService orderEmailService,
                               Clock clock) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.itemDetailsRepository = itemDetailsRepository;
        this.paymentService = paymentService;
        this.promoCodeService = promoCodeService;
        this.orderEmailService = orderEmailService;
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

        BigDecimal subtotal;
        subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            BigDecimal lineTotal = cartItem.getItemDetails().getPrice()
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

        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            appliedPromoCode = promoCodeService.validateAndCalculate(
                    request.promoCode(), user, subtotal);
            discount = promoCodeService.calculateDiscount(appliedPromoCode, subtotal);
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
                .placedAt(clock.instant())
                .build();

        orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            ItemDetails details = cartItem.getItemDetails();
            Item item = details.getItem();

            BigDecimal lineSubtotal = details.getPrice()
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

            int newQuantity = details.getStockQuantity() - cartItem.getQuantity();
            if (newQuantity < 0) {
                throw StockConflictException.forItem(details.getSku());
            }
            details.setStockQuantity(newQuantity);
            itemDetailsRepository.save(details);
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        if (appliedPromoCode != null) {
            promoCodeService.recordUsage(appliedPromoCode, user, order);
        }

        Payment payment = paymentService.createPayment(order);

        try {
            orderEmailService.sendOrderStatusEmail(order, OrderStatus.PLACED);
        } catch (Exception e) {
            // Don't fail checkout if email fails
        }

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
            BigDecimal lineTotal = cartItem.getItemDetails().getPrice()
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

        BigDecimal discount = BigDecimal.ZERO;
        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            PromoCode appliedPromoCode = promoCodeService.validateAndCalculate(
                    request.promoCode(), user, subtotal);
            discount = promoCodeService.calculateDiscount(appliedPromoCode, subtotal);
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