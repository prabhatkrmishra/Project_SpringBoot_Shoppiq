package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.dto.seller.request.SellerRegistrationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Inner item DTO for bulk seller profile creation by an administrator.
 *
 * <p>This record pairs a target user identifier with a complete seller
 * registration payload. It is used as an element within
 * {@link BulkSellerRequest} for administrative test-data population,
 * enabling administrators to onboard multiple sellers in a single
 * API call. The seller profile is created with PENDING verification
 * status at the service layer, requiring subsequent admin approval.</p>
 *
 * <p>The {@code seller} field uses cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that all
 * seller-level constraints (required business details, email format,
 * PAN length) are enforced even though the seller data is nested
 * inside this wrapper.</p>
 *
 * @param userId identifier of the existing user who will own the
 *               created seller profile; must reference a valid
 *               {@code User} entity
 * @param seller complete seller registration payload containing all
 *               required business details; validated recursively via
 *               {@link com.pkmprojects.shoppiq.dto.seller.request.SellerRegistrationRequest}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminSellerItem(
        /**
         * ID of the existing user who will own the seller profile.
         */
        @NotNull(message = "User ID is required.")
        Long userId,

        /**
         * Seller registration payload with business details.
         */
        @Valid
        @NotNull(message = "Seller registration data is required.")
        SellerRegistrationRequest seller
) {
}
