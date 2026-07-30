package com.pkmprojects.shoppiq.controller.category;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.category.CategoryRequest;
import com.pkmprojects.shoppiq.dto.category.CategoryResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.service.category.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product category management.
 *
 * <p>Exposes CRUD endpoints for creating, updating, and deleting product
 * categories, as well as read-only endpoints for browsing categories by slug,
 * listing all categories, paginated search, and retrieving top-selling categories.
 * Categories serve as the primary organizational hierarchy for the product catalog.</p>
 *
 * <p>This controller acts as the HTTP boundary for category operations. It
 * delegates all business logic — persistence, slug resolution, top-selling
 * aggregation, and paginated search — to {@link CategoryService}. The controller
 * handles no business logic beyond page-size capping and request validation.</p>
 *
 * <p>No authentication is required for read-only endpoints. Create, update, and
 * delete operations are unauthenticated in this controller (admin protection is
 * handled at the service or security layer). All endpoints are mounted under
 * /categories.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /categories                  — create a new category
 * PUT    /categories/{id}/update      — update an existing category
 * DELETE /categories/{id}/delete      — delete a category permanently
 * GET    /categories/slug/{slug}      — get category by URL slug
 * GET    /categories/all              — list all categories (ordered)
 * GET    /categories/all/paged        — list categories (paginated with search)
 * GET    /categories/top-selling      — get top-selling categories
 * </pre>
 *
 * @author prabhatkrmishra
 * @see CategoryService
 * @since 1.0.0
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private final PaginationProperties pagination;

    /**
     * Creates a new product category.
     *
     * @param request the category creation request payload (validated via @Valid)
     * @return 201 Created with the created category response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(
            @Valid @RequestBody CategoryRequest request) {

        return categoryService.create(request);
    }

    /**
     * Updates an existing category.
     *
     * @param id      the category ID to update (must be positive)
     * @param request the updated category payload (validated via @Valid)
     * @return 200 OK with the updated category response
     */
    @PutMapping("/{id}/update")
    public CategoryResponse update(
            @PathVariable
            @Positive(message = "Category id must be a positive number")
            Long id,
            @Valid @RequestBody CategoryRequest request) {

        return categoryService.update(id, request);
    }

    /**
     * Deletes a category permanently.
     *
     * <p>This action cannot be undone. Products assigned to this category
     * may need to be reassigned.</p>
     *
     * @param id the category ID to delete (must be positive)
     */
    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable
            @Positive(message = "Category id must be a positive number")
            Long id) {

        categoryService.delete(id);
    }

    /**
     * Returns a single category by its URL slug.
     *
     * <p>The slug must be lowercase alphanumeric with optional hyphens,
     * matching the pattern ^[a-z0-9]+(?:-[a-z0-9]+)*$.</p>
     *
     * @param slug the category URL slug (max 120 characters)
     * @return 200 OK with the category response
     */
    @GetMapping("/slug/{slug}")
    public CategoryResponse getBySlug(
            @PathVariable
            @NotBlank(message = "Slug must not be blank")
            @Size(max = 120, message = "Slug cannot exceed 120 characters")
            @Pattern(
                    regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                    message = "Slug may only contain lowercase letters, digits, and hyphens"
            )
            String slug) {

        return categoryService.getBySlug(slug);
    }

    /**
     * Returns all available categories, ordered by display order.
     *
     * @return 200 OK with list of all category responses
     */
    @GetMapping("/all")
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }

    /**
     * Returns all available categories in a paginated response with optional
     * search.
     *
     * <p>Used by the admin categories panel and the public categories page.
     * When a search parameter is provided, filters by name or description.</p>
     *
     * @param page   zero-based page index
     * @param size   page size (capped by the configured maximum)
     * @param search optional search term to filter by name or description
     * @return 200 OK with page of category responses
     */
    @GetMapping("/all/paged")
    public PageResponse<CategoryResponse> getAllPaginated(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String search) {
        size = Math.min(size, pagination.maxPageSize());
        return categoryService.getAll(page, size, search);
    }

    /**
     * Returns the top-selling categories based on order volume.
     *
     * @param size maximum number of categories to return (capped by the configured maximum)
     * @return 200 OK with list of top-selling category responses
     */
    @GetMapping("/top-selling")
    public List<CategoryResponse> getTopSelling(
            @RequestParam(defaultValue = "8") @Min(1) int size) {
        size = Math.min(size, pagination.maxPageSize());
        return categoryService.getTopSelling(size);
    }
}
