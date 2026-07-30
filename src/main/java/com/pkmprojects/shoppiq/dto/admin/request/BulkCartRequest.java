package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk addition of items to user carts by an administrator.
 *
 * <p>This record wraps a list of {@link AdminCartItem} entries and is
 * submitted to the admin bulk cart endpoint for populating multiple
 * user carts in a single API call. It is primarily used for
 * test-data population during development and staging, enabling
 * administrators to simulate realistic shopping activity at
 * scale.</p>
 *
 * <p>Each element in the list undergoes cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that user
 * references, item variant identifiers, and quantities meet their
 * respective constraints. The list must not be empty.</p>
 *
 * @param cartItems list of cart item creation requests, each
 *                  specifying a target user, product variant, and
 *                  quantity; must not be empty; each element is
 *                  validated recursively via {@link AdminCartItem}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkCartRequest(
        /**
         * List of cart item creation requests. Must not be empty.
         */
        @NotEmpty(message = "At least one cart item is required.")
        List<@Valid AdminCartItem> cartItems
) {
}
