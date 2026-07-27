package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.category.CategoryRequest;
import com.pkmprojects.shoppiq.dto.category.CategoryResponse;
import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.exception.general.category.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.general.category.DuplicateCategoryException;
import com.pkmprojects.shoppiq.util.SlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link CategoryService}.
 *
 * <p>
 * This service encapsulates all business rules related to category
 * management. Controllers should delegate directly to this class
 * instead of interacting with repositories.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create categories.</li>
 *     <li>Update categories.</li>
 *     <li>Delete categories.</li>
 *     <li>Retrieve categories.</li>
 *     <li>Generate unique URL slugs.</li>
 *     <li>Enforce business validation.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Entities never leave the service layer.</li>
 *     <li>DTOs form the service contract.</li>
 *     <li>All write operations execute within transactions.</li>
 *     <li>Slug generation is centralized in this service.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     */
    @Override
    public CategoryResponse create(CategoryRequest request) {

        Objects.requireNonNull(request, "Category request cannot be null.");

        validateDuplicateName(request.name());

        Category category = buildCategory(request);
        category.setSlug(generateUniqueSlug(request.name()));
        Category savedCategory = categoryWriteService.save(category);

        return CategoryResponse.fromEntity(savedCategory);
    }

    @Override
    public List<CategoryResponse> createBulk(List<CategoryRequest> requests) {
        return requests.stream().map(this::create).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     */
    @Override
    @Transactional
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
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return CategoryResponse.fromEntity(getCategoryOrThrow(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {

        Category category = categoryLookupService.findBySlug(slug).orElseThrow(() -> CategoryNotFoundException.slug(slug));

        return CategoryResponse.fromEntity(category);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
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
        List<Object[]> rows = categoryLookupService.findTopSellingCategoryIds(since, size);
        return rows.stream().map(row -> new CategoryResponse(((Number) row[0]).longValue(), (String) row[1], (String) row[2], (String) row[3])).toList();
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
