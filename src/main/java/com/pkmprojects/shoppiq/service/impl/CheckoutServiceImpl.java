package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.order.CheckoutRequest;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.order.OrderResponse;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.OrderEmailService;
import com.pkmprojects.shoppiq.service.PaymentService;
import com.pkmprojects.shoppiq.service.PromoCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
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

    public CheckoutServiceImpl(CartRepository cartRepository,
                               AddressRepository addressRepository,
                               OrderRepository orderRepository,
                               ItemDetailsRepository itemDetailsRepository,
                               PaymentService paymentService,
                               PromoCodeService promoCodeService,
                               OrderEmailService orderEmailService) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.itemDetailsRepository = itemDetailsRepository;
        this.paymentService = paymentService;
        this.promoCodeService = promoCodeService;
        this.orderEmailService = orderEmailService;
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
     */
    public CheckoutResponse checkout(User user, CheckoutRequest request) {

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(CartEmptyException::new);

        List<CartItem> cartItems = cart.getItems();

        if (cartItems == null || cartItems.isEmpty()) {
            throw new CartEmptyException();
        }

        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> AddressNotFoundException.id(request.addressId()));

        if (!address.getUser().getId().equals(user.getId())) {
            throw AddressAccessDeniedException.forAddress(request.addressId());
        }

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
        PromoCode appliedPromoCode = null;

        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            appliedPromoCode = promoCodeService.validateAndCalculate(
                    request.promoCode(), user, subtotal);
            discount = promoCodeService.calculateDiscount(appliedPromoCode, subtotal);
        }

        BigDecimal grandTotal = subtotal.add(shippingFee).add(tax).subtract(discount);

        Order order = Order.builder()
                .user(user)
                .address(address)
                .shippingAddress(OrderAddressSnapshot.from(address))
                .status(OrderStatus.PLACED)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .tax(tax)
                .discount(discount)
                .grandTotal(grandTotal)
                .promoCode(appliedPromoCode)
                .placedAt(Instant.now())
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
                    .itemNameSnapshot(item.getName())
                    .unitPriceSnapshot(details.getPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(lineSubtotal)
                    .build();

            order.addOrderItem(orderItem);

            details.setStockQuantity(details.getStockQuantity() - cartItem.getQuantity());
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
     * Cancels an order that must be in {@code PLACED} status.
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

        order.setStatus(OrderStatus.CANCELLED);
    }

    // =========================================================
    // Return
    // =========================================================

    /**
     * Requests a return for an order in {@code DELIVERED} status.
     *
     * <p>Restores stock for each order item and sets the order status to {@code RETURNED}.</p>
     *
     * @param user    authenticated customer
     * @param orderId target order id
     */
    public void requestReturn(User user, Long orderId) {
        Order order = findOrderOrThrow(orderId);
        assertOwnership(user, order);

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException(orderId, order.getStatus());
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getItemDetails() != null) {
                ItemDetails details = orderItem.getItemDetails();
                details.setStockQuantity(details.getStockQuantity() + orderItem.getQuantity());
                itemDetailsRepository.save(details);
            }
        }

        order.setStatus(OrderStatus.RETURNED);
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