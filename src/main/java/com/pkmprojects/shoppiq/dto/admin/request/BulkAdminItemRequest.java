package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk-importing multiple product items via the admin panel.
 *
 * <p>This record wraps a list of {@link AdminItemRequest} entries and
 * is submitted to the admin bulk item import endpoint for creating
 * multiple catalog products in a single API call. It supports batch
 * product onboarding scenarios where administrators need to populate
 * the catalog from spreadsheets or external data sources.</p>
 *
 * <p>Each element in the list undergoes cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that all product
 * fields (name, SKU, pricing, stock, category, seller) meet their
 * respective constraints. The list must not be empty, as a bulk
 * import with zero items is a no-op.</p>
 *
 * @param items list of item creation requests, each containing all
 *              required product fields; must not be empty; each element
 *              is validated recursively via {@link AdminItemRequest}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkAdminItemRequest(
        /**
         * List of item creation requests. Must not be empty.
         */
        @NotEmpty(message = "At least one item is required.")
        List<@Valid AdminItemRequest> items
) {
}
