package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkAdminItemRequest(
        @NotEmpty(message = "At least one item is required.")
        List<@Valid AdminItemRequest> items
) {
}
