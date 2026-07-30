package com.pkmprojects.shoppiq.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a product category.
 *
 * <p>This record is submitted to the category management endpoints
 * ({@code POST /api/category}, {@code PUT /api/category/{id}}) and
 * carries the data needed to define or modify a product category. The
 * {@code name} field is used to generate a URL-friendly slug via
 * {@link com.pkmprojects.shoppiq.util.SlugUtil} at the service layer,
 * ensuring clean and SEO-friendly category URLs.</p>
 *
 * <p>The slug is stored separately to preserve the original name for
 * display while enabling clean URLs for browsing. Category names
 * should be unique across the catalog, and the service layer enforces
 * this constraint before persistence. The optional {@code description}
 * is displayed on category landing pages to help customers understand
 * what products they will find in the category.</p>
 *
 * @param name        human-readable category name, required, max 100 characters;
 *                    used for display and slug generation; should be unique
 *                    across the catalog
 * @param description optional long-form description of the category,
 *                    max 255 characters; displayed on the category
 *                    landing page to provide context to customers
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CategoryRequest(

        /**
         * Human-readable category name.
         *
         * <p>Required. Must be unique across all categories.
         * Used to generate the URL-friendly slug.</p>
         *
         * @see com.pkmprojects.shoppiq.util.SlugUtil#generate(String)
         */
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name cannot exceed 100 characters")
        String name,

        /**
         * Optional long-form description of the category.
         *
         * <p>Maximum 255 characters. Displayed on category landing pages
         * to help customers understand what products they'll find.</p>
         */
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description

) {
}
