package com.pkmprojects.shoppiq.dto.request;

import com.pkmprojects.shoppiq.dto.seller.request.SellerRegistrationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Inner item DTO used by {@link BulkSellerRequest} for bulk seller creation.
 *
 * <p>
 * Each item specifies the target user and the seller registration details.
 * The seller is created with PENDING verification status (same as the
 * customer-facing registration flow).
 * </p>
 *
 * @param userId ID of the existing user who will own the seller profile
 * @param seller seller registration payload
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AdminSellerItem(
        @NotNull(message = "User ID is required.")
        Long userId,

        @Valid
        @NotNull(message = "Seller registration data is required.")
        SellerRegistrationRequest seller
) {
}
