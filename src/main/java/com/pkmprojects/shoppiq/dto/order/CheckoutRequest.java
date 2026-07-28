package com.pkmprojects.shoppiq.dto.order;

import com.pkmprojects.shoppiq.enums.DeliveryType;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for placing an order at checkout.
 *
 * <p>This Java record demonstrates <b>enum validation</b> using Jakarta
 * Validation. The {@code paymentMethod} and {@code deliveryType} fields
 * accept enum values that Spring Boot automatically deserializes from JSON
 * strings. Invalid values result in a {@code HttpMessageNotReadableException}
 * with a descriptive error.</p>
 *
 * <p><b>Optional field:</b> {@code promoCode} is nullable — if not provided,
 * no discount is applied. This is a common pattern for conditional fields
 * in request DTOs.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CheckoutRequest(

        @NotNull(message = "Address ID is required.")
        Long addressId,

        @NotNull(message = "Payment method is required.")
        PaymentMethod paymentMethod,

        @NotNull(message = "Delivery type is required.")
        DeliveryType deliveryType,

        String promoCode
) {
}
