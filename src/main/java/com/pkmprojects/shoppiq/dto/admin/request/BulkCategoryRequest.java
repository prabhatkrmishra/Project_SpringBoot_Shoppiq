package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.dto.category.CategoryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk-importing multiple categories via the admin panel.
 *
 * <p>This record wraps a list of {@link com.pkmprojects.shoppiq.dto.category.CategoryRequest}
 * entries and is submitted to the admin bulk category endpoint for
 * creating multiple product categories in a single API call. It
 * supports batch catalog setup scenarios where administrators need
 * to provision the category hierarchy from external data sources
 * or spreadsheets.</p>
 *
 * <p>Each element in the list undergoes cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that category
 * names and descriptions meet their respective constraints. URL
 * slugs are auto-generated from the name at the service layer.
 * The list must not be empty.</p>
 *
 * @param categories list of category creation requests, each
 *                   containing a name and optional description;
 *                   must not be empty; each element is validated
 *                   recursively via
 *                   {@link com.pkmprojects.shoppiq.dto.category.CategoryRequest}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkCategoryRequest(
        /**
         * List of category creation requests. Must not be empty.
         */
        @NotEmpty(message = "At least one category is required.")
        List<@Valid CategoryRequest> categories
) {
}
