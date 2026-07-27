package com.pkmprojects.shoppiq.events;

import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.service.order.OrderEmailService;
import com.pkmprojects.shoppiq.service.promo.PromoCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles post-checkout side effects after an order is placed.
 *
 * <p>This listener subscribes to {@link OrderPlacedEvent} and performs
 * two side effect operations that were previously embedded inline in
 * {@code CheckoutServiceImpl.doCheckout()}:</p>
 * <ol>
 *     <li><strong>Promo code usage recording</strong> — increments the
 *         global usage counter and creates a per-user usage record via
 *         {@link PromoCodeService#recordUsage}. Only runs when a promo
 *         code was applied.</li>
 *     <li><strong>Order confirmation email</strong> — sends a "placed"
 *         email to the customer via {@link OrderEmailService}.</li>
 * </ol>
 *
 * <h2>Async Execution</h2>
 * <p>This listener is annotated {@code @Async}, meaning it runs on a
 * separate thread from the checkout transaction. This ensures that:</p>
 * <ul>
 *     <li>Checkout response time is not blocked by email SMTP calls.</li>
 *     <li>Email failures do not affect the checkout user experience.</li>
 *     <li>Promo recording failures are logged but do not prevent
 *         order creation.</li>
 * </ul>
 *
 * <h2>Error Handling</h2>
 * <p>Both operations are wrapped in try/catch blocks. Failures are
 * logged at {@code WARN} level and swallowed — the order has already
 * been committed successfully at this point. This is a deliberate
 * design choice: side effects should never roll back the primary
 * transaction.</p>
 *
 * <h2>Transaction Semantics</h2>
 * <p>Because this listener is {@code @Async}, it executes outside the
 * checkout transaction. Each operation ({@code recordUsage},
 * {@code sendEmail}) runs in its own implicit transaction managed by
 * Spring's async infrastructure.</p>
 *
 * @author PrabhatKrMishra
 * @see OrderPlacedEvent
 * @since 1.4.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedEventListener {

    private final OrderEmailService orderEmailService;
    private final PromoCodeService promoCodeService;

    /**
     * Handles an {@link OrderPlacedEvent} asynchronously.
     *
     * <p>Records promo usage first (data integrity), then sends
     * the confirmation email (best-effort).</p>
     *
     * @param event the order-placed event carrying the order, user,
     *              and applied promo code
     */
    @Async
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        recordPromoUsage(event);
        sendEmail(event);
    }

    /**
     * Records that a promo code was used on this order.
     *
     * <p>Skipped when no promo code was applied. Increments the
     * global usage counter and creates a per-user usage record.
     * Failures are logged but do not propagate.</p>
     *
     * @param event the order-placed event
     */
    private void recordPromoUsage(OrderPlacedEvent event) {
        if (event.appliedPromoCode() == null) {
            return;
        }
        try {
            promoCodeService.recordUsage(
                    event.appliedPromoCode(), event.user(), event.order());
            log.debug("Promo usage recorded: code={}, orderId={}",
                    event.appliedPromoCode().getCode(), event.order().getId());
        } catch (Exception e) {
            log.warn("Failed to record promo usage for order {}: {}",
                    event.order().getId(), e.getMessage());
        }
    }

    /**
     * Sends an order confirmation email to the customer.
     *
     * <p>Delegates to {@link OrderEmailService} with status
     * {@link OrderStatus#PLACED}. Failures are logged but
     * do not propagate.</p>
     *
     * @param event the order-placed event
     */
    private void sendEmail(OrderPlacedEvent event) {
        try {
            orderEmailService.sendOrderStatusEmail(event.order(), OrderStatus.PLACED);
        } catch (Exception e) {
            log.warn("Failed to send placed-email for order {}: {}",
                    event.order().getId(), e.getMessage());
        }
    }
}
