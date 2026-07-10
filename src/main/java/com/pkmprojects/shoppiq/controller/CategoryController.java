package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.request.CategoryRequest;
import com.pkmprojects.shoppiq.dto.response.CategoryResponse;
import com.pkmprojects.shoppiq.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing product categories.
 *
 * <p>
 * Exposes CRUD endpoints for category management. The controller acts purely
 * as the HTTP boundary, delegating all business logic to
 * {@link CategoryService}.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Receive HTTP requests.</li>
 *     <li>Validate request payloads.</li>
 *     <li>Delegate business operations to the service layer.</li>
 *     <li>Return DTOs suitable for API consumers.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Contains no business logic.</li>
 *     <li>Contains no persistence logic.</li>
 *     <li>Always communicates using DTOs.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    /**
     * Service responsible for category operations.
     */
    private final CategoryService categoryService;

    private final PaginationProperties pagination;

    /**
     * Creates a new category.
     *
     * @param request category creation request
     * @return created category
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
     * @param id      category identifier — must be a positive number
     * @param request updated category information
     * @return updated category
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
     * Deletes a category.
     *
     * @param id category identifier — must be a positive number
     */
    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable
            @Positive(message = "Category id must be a positive number")
            Long id) {

        categoryService.delete(id);
    }

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
     * Retrieves all available categories.
     *
     * @return ordered category list
     */
    @GetMapping("/all")
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }

    /**
     * Retrieves all available categories, paginated.
     *
     * <p>Used by the admin categories panel and the public categories page.</p>
     *
     * @param page   zero-based page index
     * @param size   requested page size
     * @param search optional search term to filter by name or description
     * @return paginated category responses
     */
    @GetMapping("/all/paged")
    public PageResponse<CategoryResponse> getAllPaginated(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String search) {
        size = Math.min(size, pagination.maxPageSize());
        return categoryService.getAll(page, size, search);
    }
}