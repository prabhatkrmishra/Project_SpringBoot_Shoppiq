package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.dto.category.CategoryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Request DTO for bulk-importing multiple categories via the admin panel.
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkCategoryRequest(
        @NotEmpty(message = "At least one category is required.")
        List<@Valid CategoryRequest> categories
) {
}
