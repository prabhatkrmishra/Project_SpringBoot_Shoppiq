package com.pkmprojects.shoppiq.dto.request;

import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Inner item DTO used by {@link BulkAddressRequest} for bulk address creation.
 *
 * <p>
 * Each item specifies a target user ID and the address details to create
 * for that user.
 * </p>
 *
 * @param userId  ID of the existing user who will own the address
 * @param address address creation payload
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AdminAddressItem(
        @NotNull(message = "User ID is required.")
        Long userId,

        @Valid
        @NotNull(message = "Address data is required.")
        CreateAddressRequest address
) {
}
