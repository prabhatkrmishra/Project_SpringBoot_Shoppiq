package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk-importing multiple product items via the admin panel.
 *
 * <p>Wraps a list of {@link AdminItemRequest} entries, all of which must
 * pass individual validation for the bulk operation to proceed.</p>
 *
 * <p><b>Why a wrapper record?</b> Spring MVC cannot validate
 * {@code List<AdminItemRequest>} directly as a request body. Wrapping the
 * list in a record with {@code @NotEmpty} + type-use {@code @Valid} enables
 * proper cascading validation and eliminates Hibernate Validator warnings.</p>
 *
 * @param items the list of item requests to import (must not be empty)
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkAdminItemRequest(
        @NotEmpty(message = "At least one item is required.")
        List<@Valid AdminItemRequest> items
) {
}
