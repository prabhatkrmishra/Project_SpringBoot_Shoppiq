package com.pkmprojects.shoppiq.dto.admin.request;

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
 * <p><b>Validation pattern:</b> Cascading validation via {@code @Valid} on the
 * nested {@link com.pkmprojects.shoppiq.dto.address.CreateAddressRequest} ensures
 * that both the wrapper and the inner DTO are validated together. This is a
 * common pattern for composite request DTOs in Spring Boot.</p>
 *
 * @param userId  ID of the existing user who will own the address
 * @param address address creation payload
 * @author prabhatkrmishra
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
