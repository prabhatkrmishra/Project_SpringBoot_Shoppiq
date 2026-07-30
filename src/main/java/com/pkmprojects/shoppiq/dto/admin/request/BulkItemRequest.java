package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.dto.item.ItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk creation of products by a seller.
 *
 * <p>This record wraps a list of {@link com.pkmprojects.shoppiq.dto.item.ItemRequest}
 * entries and is submitted to the bulk item creation endpoint for
 * creating multiple catalog products in a single API call. Unlike
 * {@link BulkAdminItemRequest}, this DTO does not include a
 * {@code sellerId} field because the seller identity is derived from
 * the authenticated user's security context.</p>
 *
 * <p>Each element in the list undergoes cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that all product
 * fields meet their respective constraints. The list must not be
 * empty. Updates to existing products are handled individually
 * through the standard item update endpoint.</p>
 *
 * @param items list of item creation requests, each containing all
 *              required product fields; must not be empty; each element
 *              is validated recursively via
 *              {@link com.pkmprojects.shoppiq.dto.item.ItemRequest}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkItemRequest(

        /**
         * List of item creation requests.
         */
        @NotEmpty(message = "At least one item is required.")
        List<@Valid ItemRequest> items
) {
}
