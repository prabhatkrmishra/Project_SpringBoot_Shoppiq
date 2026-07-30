package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Inner item DTO for bulk order creation by an administrator.
 *
 * <p>This record specifies the parameters needed to place an order on
 * behalf of a specific user. It is used as an element within
 * {@link BulkOrderRequest} for administrative test-data population,
 * enabling administrators to create orders for multiple users in a
 * single API call. The order is constructed from the user's existing
 * cart contents at the service layer.</p>
 *
 * <p>The {@code addressId} must reference an address that already
 * exists in the target user's address book. The service layer
 * verifies ownership before proceeding. The {@code paymentMethod}
 * determines whether COD surcharges and delivery restrictions apply.</p>
 *
 * @param userId        identifier of the existing user who will own the
 *                      created order; must reference a valid {@code User} entity
 * @param addressId     identifier of a shipping address belonging to
 *                      that user; must reference an existing {@code Address}
 *                      entity owned by the user specified by {@code userId}
 * @param paymentMethod payment method for the order, e.g. CREDIT_CARD,
 *                      UPI, or COD; determines applicable surcharges
 *                      and payment gateway routing
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminOrderItem(
        /**
         * ID of the existing user who will own the order.
         */
        @NotNull(message = "User ID is required.")
        Long userId,

        /**
         * ID of an existing address belonging to that user.
         */
        @NotNull(message = "Address ID is required.")
        Long addressId,

        /**
         * Payment method for the order.
         */
        @NotNull(message = "Payment method is required.")
        PaymentMethod paymentMethod
) {
}
