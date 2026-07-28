package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.category.CategoryRequest;
import com.pkmprojects.shoppiq.dto.category.CategoryResponse;
import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.exception.general.category.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.general.category.DuplicateCategoryException;
import com.pkmprojects.shoppiq.util.SlugUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link CategoryService}
 * containing business logic for category management.
 *
 * <p>Creates, updates, deletes, and retrieves categories with duplicate-name
 * validation and unique slug generation. Used by {@code CategoryController}.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Write operations are atomic; reads use {@code readOnly = true}.</li>
 *   <li><strong>Constructor injection</strong> — final fields for immutability and testability.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see CategoryService
 * @since 1.0.0
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    /**
     * Lookup service for category queries.
     */
    private final CategoryLookupService categoryLookupService;
    private final CategoryWriteService categoryWriteService;
    private final Clock clock;

    /**
     * Creates a new service instance.
     *
     * @param categoryLookupService category lookup service
     * @param categoryWriteService  category write service
     * @param clock                 clock for deterministic time in business logic
     */
    public CategoryServiceImpl(CategoryLookupService categoryLookupService,
                               CategoryWriteService categoryWriteService, Clock clock) {
        this.categoryLookupService = categoryLookupService;
        this.categoryWriteService = categoryWriteService;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Evicts the categories cache to ensure fresh data on next read.</p>
     */
    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse create(CategoryRequest request) {

        Objects.requireNonNull(request, "Category request cannot be null.");

        validateDuplicateName(request.name());

        Category category = buildCategory(request);
        return saveWithSlugRetry(category, request.name());
    }

    /**
     * Saves the category with retry-on-conflict for slug collisions (BUG-0023).
     *
     * <p>Follows the same pattern as {@code ItemServiceImpl.saveWithSlugRetry()}:
     * if a {@link DataIntegrityViolationException} caused by a unique-slug
     * constraint is caught, a new slug is generated and the save is retried
     * (up to 10 attempts).</p>
     */
    private CategoryResponse saveWithSlugRetry(Category category, String name) {
        int attempts = 0;
        while (attempts < 10) {
            category.setSlug(generateUniqueSlug(name));
            try {
                Category saved = categoryWriteService.save(category);
                return CategoryResponse.fromEntity(saved);
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage() != null && e.getMessage().contains("slug")) {
                    attempts++;
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Failed to generate unique slug after 10 attempts");
    }

    /**
     * Creates multiple categories in bulk by delegating to {@link #create}.
     *
     * <p>Evicts the categories cache to ensure fresh data on next read.</p>
     *
     * @param requests list of category creation requests
     * @return list of created category responses
     */
    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public List<CategoryResponse> createBulk(List<CategoryRequest> requests) {
        return requests.stream().map(this::create).toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Evicts the categories cache to ensure fresh data on next read.</p>
     */
    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse update(Long id, CategoryRequest request) {

        Objects.requireNonNull(request, "Category request cannot be null.");

        Category category = getCategoryOrThrow(id);

        if (!category.getName().equalsIgnoreCase(request.name())) {
            if (categoryLookupService.existsByNameAndIdNot(request.name(), id)) {
                throw DuplicateCategoryException.category(request.name());
            }
            category.setName(request.name());
            category.setSlug(generateUniqueSlug(request.name()));
        }
        category.setDescription(request.description());

        Category updatedCategory = categoryWriteService.save(category);

        return CategoryResponse.fromEntity(updatedCategory);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Evicts the categories cache to ensure fresh data on next read.</p>
     */
    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id) {

        Category category = getCategoryOrThrow(id);

        /*
         * Phase 2+
         *
         * Prevent deletion if products reference this category.
         *
         * Example:
         *
         * if (itemRepository.existsByCategory(category)) {
         *     throw new InvalidOperationException(...);
         * }
         */

        categoryWriteService.delete(category);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Results are cached to reduce database load on category lookups.</p>
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable("categories")
    public CategoryResponse getById(Long id) {
        return CategoryResponse.fromEntity(getCategoryOrThrow(id));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Results are cached to reduce database load on category lookups.</p>
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable("categories")
    public CategoryResponse getBySlug(String slug) {

        Category category = categoryLookupService.findBySlug(slug).orElseThrow(() -> CategoryNotFoundException.slug(slug));

        return CategoryResponse.fromEntity(category);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Results are cached to reduce database load on category listings.</p>
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable("categories")
    public java.util.List<CategoryResponse> getAll() {

        return categoryLookupService.findAll().stream().map(CategoryResponse::fromEntity).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getAll(int page, int size) {

        return PageResponse.of(categoryLookupService.findAll(page, size), CategoryResponse::fromEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getAll(int page, int size, String search) {

        var categoryPage = (search != null && !search.isBlank()) ? categoryLookupService.findByNameOrDescriptionContaining(search, page, size) : categoryLookupService.findAll(page, size);
        return PageResponse.of(categoryPage, CategoryResponse::fromEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CategoryResponse> getTopSelling(int size) {
        Instant since = clock.instant().minus(30, ChronoUnit.DAYS);
        return categoryLookupService.findTopSellingCategoryIds(since, size).stream()
                .map(row -> new CategoryResponse(row.getId(), row.getName(), row.getSlug(), row.getDescription()))
                .toList();
    }

    /**
     * Retrieves a category by its identifier.
     *
     * <p>
     * This helper centralizes lookup logic to ensure all service methods
     * throw a consistent exception when a category cannot be found.
     * </p>
     *
     * @param id category identifier
     * @return managed category entity
     * @throws CategoryNotFoundException if no category exists with the given id
     */
    private Category getCategoryOrThrow(Long id) {

        return categoryLookupService.findById(id).orElseThrow(() -> CategoryNotFoundException.id(id));
    }

    /**
     * Validates that no existing category already uses the supplied name.
     *
     * @param name category name
     * @throws DuplicateCategoryException if another category already exists
     */
    private void validateDuplicateName(String name) {

        if (categoryLookupService.existsByName(name)) {
            throw DuplicateCategoryException.category(name);
        }
    }

    /**
     * Generates a unique URL-friendly slug.
     *
     * <p>
     * The initial slug is produced by {@link SlugUtil}. If another category
     * already uses the same slug, numeric suffixes are appended until a
     * unique slug is found.
     * </p>
     *
     * <h4>Example</h4>
     *
     * <pre>
     * electronics
     * electronics-2
     * electronics-3
     * </pre>
     *
     * @param categoryName category name
     * @return unique slug
     */
    private String generateUniqueSlug(String categoryName) {

        String baseSlug = SlugUtil.toSlug(categoryName);
        String slug = baseSlug;
        int counter = 2;

        while (categoryLookupService.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    /**
     * Creates a new {@link Category} entity from the supplied request.
     *
     * <p>
     * The returned entity is transient and has not yet been persisted.
     * The slug is intentionally left unset and should be assigned by
     * {@link #generateUniqueSlug(String)} before saving.
     * </p>
     *
     * @param request category request
     * @return new category entity
     */
    private Category buildCategory(CategoryRequest request) {
        return Category.builder().name(request.name()).description(request.description()).build();
    }
}
