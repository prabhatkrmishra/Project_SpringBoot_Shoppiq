package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.service.impl.CheckoutServiceImpl;

/**
 * Delivery speed options available at checkout.
 *
 * <p>Each type maps to a fixed shipping fee calculated in
 * {@link CheckoutServiceImpl}.</p>
 *
 * @author PrabhatKrMishra
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
