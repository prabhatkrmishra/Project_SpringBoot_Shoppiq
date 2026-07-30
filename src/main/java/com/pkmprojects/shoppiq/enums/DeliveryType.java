package com.pkmprojects.shoppiq.enums;

/**
 * Delivery speed options available at checkout.
 *
 * <p>This enum defines the delivery speed choices presented to customers
 * during the checkout flow. {@link #NORMAL} provides free standard
 * shipping with a typical delivery window of 3-7 business days.
 * {@link #EXPRESS_1DAY} incurs an additional shipping charge (configured
 * in {@link com.pkmprojects.shoppiq.config.CheckoutProperties}) and
 * guarantees delivery within one business day.</p>
 *
 * <p>The delivery type affects the order total calculation and the
 * shipping logistics. Express orders are prioritized in the fulfillment
 * pipeline and routed through expedited shipping carriers.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum DeliveryType {

    /**
     * Standard delivery — free shipping.
     */
    NORMAL,

    /**
     * Express 1-day delivery — additional shipping charge.
     */
    EXPRESS_1DAY
}
