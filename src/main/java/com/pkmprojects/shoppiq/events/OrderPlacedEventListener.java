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
 * Async event listener that handles post-checkout side effects after an order is placed.
 *
 * <p>Records promo code usage and sends order confirmation emails.
 * This listener is marked with {@code @Async} to execute in a separate
 * thread pool, ensuring that the checkout response is not blocked by
 * email delivery or database updates for promo usage.</p>
 *
 * <p>The listener processes events in order: first recording promo usage
 * (data integrity concern), then sending the confirmation email
 * (best-effort concern). Failures in either operation are logged but
 * do not propagate to the event publisher.</p>
 *
 * @author prabhatkrmishra
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
