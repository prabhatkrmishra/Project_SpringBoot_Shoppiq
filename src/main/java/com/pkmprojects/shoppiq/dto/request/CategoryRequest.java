package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a category.
 *
 * <p>Used in {@code CategoryController} to receive category data
 * and validate it before passing to the service layer.</p>
 *
 * <h4>Validation rules</h4>
 * <ul>
 *   <li>{@code name} — required, max 100 characters, automatically
 *       converted to a unique slug</li>
 *   <li>{@code description} — optional, max 255 characters</li>
 * </ul>
 *
 * <h4>Slug generation</h4>
 * <p>The {@code name} field is used to generate a URL-friendly {@code slug}
 * in the service layer using {@link com.pkmprojects.shoppiq.util.SlugUtil}.
 * The slug is stored separately to preserve the original name for display
 * while enabling clean URLs for browsing.</p>
 *
 * <h4>Usage</h4>
 * <pre>
 * POST /api/category
 * {
 *   "name": "Electronics & Gadgets",
 *   "description": "All things electronic"
 * }
 * </pre>
 * <p>This would generate a category with {@code name="Electronics & Gadgets"}
 * and {@code slug="electronics-gadgets"}.</p>
 *
 * @see com.pkmprojects.shoppiq.entity.Category
 * @see com.pkmprojects.shoppiq.controller.CategoryController
 * @see com.pkmprojects.shoppiq.service.CategoryService
 */
public record CategoryRequest(

        /*
          Human-readable category name.

          <p>Required. Must be unique across all categories.
          Used to generate the URL-friendly slug.</p>

          @see com.pkmprojects.shoppiq.util.SlugUtil#generate(String)
         */
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name cannot exceed 100 characters")
        String name,

        /*
          Optional long-form description of the category.

          <p>Maximum 255 characters. Displayed on category landing pages
          to help customers understand what products they'll find.</p>
         */
        @NotBlank(message = "Category description cannot be blank")
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description

) {
}