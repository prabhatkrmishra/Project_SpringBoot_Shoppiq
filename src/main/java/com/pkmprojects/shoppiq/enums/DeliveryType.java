package com.pkmprojects.shoppiq.enums;

import com.pkmprojects.shoppiq.service.checkout.CheckoutServiceImpl;

/**
 * <strong>Spring Boot Concept:</strong> Delivery speed options available at checkout.
 *
 * <p>Each type maps to a fixed shipping fee calculated in
 * {@link CheckoutServiceImpl}.</p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Enum-driven rate calculation</strong> — The checkout
 *         service uses this enum to determine shipping fees (free for
 *         NORMAL, chargeable for EXPRESS_1DAY). This keeps the rate
 *         logic in the service layer while the enum provides type-safe
 *         option selection.</li>
 *     <li><strong>Stored as STRING in the database</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the {@link Order} entity
 *         for readable database values.</li>
 * </ul>
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
