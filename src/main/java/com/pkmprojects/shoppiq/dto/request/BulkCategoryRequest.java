package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkCategoryRequest(
        @NotEmpty(message = "At least one category is required.")
        @Valid
        List<CategoryRequest> categories
) {
}
