package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk creation of addresses by an administrator.
 *
 * <p>This record wraps a list of {@link AdminAddressItem} entries and
 * is submitted to the admin bulk address endpoint for creating
 * multiple user addresses in a single API call. It is primarily used
 * for test-data population during development and staging, and for
 * data migration scenarios where addresses need to be provisioned
 * at scale.</p>
 *
 * <p>Each element in the list undergoes cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that both the
 * user reference and the nested address payload meet all
 * constraints. The list itself must not be empty, as bulk operations
 * with zero items would be a no-op.</p>
 *
 * @param addresses list of address creation requests, each containing
 *                  a target user ID and complete address details;
 *                  must not be empty; each element is validated
 *                  recursively via {@link AdminAddressItem}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkAddressRequest(
        /**
         * List of address creation requests. Must not be empty.
         */
        @NotEmpty(message = "At least one address is required.")
        List<@Valid AdminAddressItem> addresses
) {
}
