package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk creation of seller profiles by an administrator.
 *
 * <p>This record wraps a list of {@link AdminSellerItem} entries and is
 * submitted to the admin bulk seller endpoint for onboarding multiple
 * sellers in a single API call. It is primarily used for test-data
 * population during development and staging, enabling administrators
 * to provision the seller ecosystem at scale.</p>
 *
 * <p>Each element in the list undergoes cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that user
 * references and seller registration details meet their respective
 * constraints. Sellers created through this endpoint are assigned
 * PENDING verification status at the service layer. The list must
 * not be empty.</p>
 *
 * @param sellers list of seller creation requests, each specifying a
 *                target user and complete seller registration payload;
 *                must not be empty; each element is validated
 *                recursively via {@link AdminSellerItem}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkSellerRequest(
        /**
         * List of seller creation requests. Must not be empty.
         */
        @NotEmpty(message = "At least one seller is required.")
        List<@Valid AdminSellerItem> sellers
) {
}
