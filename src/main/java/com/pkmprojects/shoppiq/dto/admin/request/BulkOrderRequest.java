package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk creation of orders by an administrator.
 *
 * <p>This record wraps a list of {@link AdminOrderItem} entries and is
 * submitted to the admin bulk order endpoint for creating multiple
 * orders in a single API call. It is primarily used for test-data
 * population during development and staging, enabling administrators
 * to simulate realistic order volumes at scale.</p>
 *
 * <p>Each element in the list undergoes cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that user
 * references, address references, and payment methods meet their
 * respective constraints. The list must not be empty. Orders are
 * constructed from each user's existing cart contents at the service
 * layer.</p>
 *
 * @param orders list of order creation requests, each specifying a
 *               target user, shipping address, and payment method;
 *               must not be empty; each element is validated
 *               recursively via {@link AdminOrderItem}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkOrderRequest(
        /**
         * List of order creation requests. Must not be empty.
         */
        @NotEmpty(message = "At least one order is required.")
        List<@Valid AdminOrderItem> orders
) {
}
