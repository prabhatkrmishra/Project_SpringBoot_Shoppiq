package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Inner item DTO for bulk address creation by an administrator.
 *
 * <p>This record pairs a target user identifier with a full address
 * creation payload. It is used as an element within
 * {@link BulkAddressRequest} to allow administrators to create
 * addresses for multiple users in a single API call, which is
 * primarily useful for test-data population and data migration
 * scenarios.</p>
 *
 * <p>The {@code address} field uses cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that all
 * address-level constraints (required fields, length limits, phone
 * format) are enforced even though the address is nested inside
 * this wrapper. The service layer verifies that the {@code userId}
 * references an existing user before persisting.</p>
 *
 * @param userId  identifier of the existing user who will own the
 *                created address; must reference a valid {@code User} entity
 * @param address complete address creation payload containing all
 *                required address fields; validated recursively via
 *                {@link com.pkmprojects.shoppiq.dto.address.CreateAddressRequest}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminAddressItem(
        /**
         * ID of the existing user who will own the address.
         */
        @NotNull(message = "User ID is required.")
        Long userId,

        /**
         * Address creation payload with full address details.
         */
        @Valid
        @NotNull(message = "Address data is required.")
        CreateAddressRequest address
) {
}
