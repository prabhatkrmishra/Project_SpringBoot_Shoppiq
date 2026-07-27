package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.dto.category.CategoryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkCategoryRequest(
        @NotEmpty(message = "At least one category is required.")
        List<@Valid CategoryRequest> categories
) {
}
