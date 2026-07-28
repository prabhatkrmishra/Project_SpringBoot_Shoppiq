package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Inner item DTO used by {@link BulkOrderRequest} for bulk order creation.
 *
 * <p>
 * Each item specifies the target user, their shipping address ID, and the
 * chosen payment method. The order is created from the user's existing cart
 * contents, following the same checkout flow as the customer-facing endpoint.
 * </p>
 *
 * <p><b>Validation pattern:</b> {@code @NotNull} on the enum field
 * {@code paymentMethod} ensures the client always provides a valid
 * {@link com.pkmprojects.shoppiq.enums.PaymentMethod} value. Spring Boot
 * automatically deserializes JSON strings to enums and rejects invalid
 * values with a clear error.</p>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Users with missing or empty carts will be skipped.</li>
 *     <li>Address ownership is validated at the service layer.</li>
 * </ul>
 *
 * @param userId        ID of the existing user who will own the order
 * @param addressId     ID of an existing address belonging to that user
 * @param paymentMethod payment method for the order
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminOrderItem(
        @NotNull(message = "User ID is required.")
        Long userId,

        @NotNull(message = "Address ID is required.")
        Long addressId,

        @NotNull(message = "Payment method is required.")
        PaymentMethod paymentMethod
) {
}
