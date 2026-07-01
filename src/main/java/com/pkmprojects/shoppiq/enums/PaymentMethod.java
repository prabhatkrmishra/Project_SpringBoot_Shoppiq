package com.pkmprojects.shoppiq.enums;

/**
 * Supported payment methods for an {@link com.pkmprojects.shoppiq.entity.Order}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public enum PaymentMethod {

    /**
     * Cash on delivery.
     */
    COD,

    /**
     * Online payment (card, UPI, wallet, etc.).
     */
    ONLINE
}
