package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.repository.category.CategoryRepository;
import com.pkmprojects.shoppiq.repository.category.projection.CategorySalesRanking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * {@link CategoryLookupService} implementation providing read-only category queries.
 *
 * @author prabhatkrmishra
 * @see CategoryLookupService
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CategoryLookupServiceImpl implements CategoryLookupService {

    private final CategoryRepository categoryRepository;

    /**
     * Finds a category by its database identifier.
     *
     * @param categoryId the category ID
     * @return optional containing the category if found
     */
    @Override
    public Optional<Category> findById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    /**
     * Finds a category by its URL-friendly slug.
     *
     * @param slug the category slug
     * @return optional containing the category if found
     */
    @Override
    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    /**
     * Checks whether a category exists with the given name (case-insensitive).
     *
     * @param name the category name
     * @return true if a category with that name exists
     */
    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    /**
     * Checks whether a category exists with the given name excluding a specific ID.
     *
     * @param name the category name
     * @param id   the category ID to exclude
     * @return true if another category with that name exists
     */
    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    /**
     * Checks whether a category exists with the given slug.
     *
     * @param slug the category slug
     * @return true if a category with that slug exists
     */
    @Override
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    /**
     * Returns the total number of categories.
     *
     * @return total category count
     */
    @Override
    public long count() {
        return categoryRepository.count();
    }

    /**
     * Returns all categories ordered by name ascending.
     *
     * @return list of all categories
     */
    @Override
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    /**
     * Retrieves a paginated list of categories ordered by name ascending.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated category results
     */
    @Override
    public Page<Category> findAll(int page, int size) {
        return categoryRepository.findAllByOrderByNameAsc(PageRequest.of(page, size));
    }

    /**
     * Searches categories by name or description containing the search term.
     *
     * @param search the search term (case-insensitive)
     * @param page   zero-based page index
     * @param size   page size
     * @return paginated category results matching the search
     */
    @Override
    public Page<Category> findByNameOrDescriptionContaining(String search, int page, int size) {
        return categoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                search, search, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    /**
     * Finds top-selling category IDs based on order volume since the given timestamp.
     *
     * @param since start timestamp (inclusive)
     * @param size  maximum number of results
     * @return list of category sales ranking projections
     */
    @Override
    public List<CategorySalesRanking> findTopSellingCategoryIds(Instant since, int size) {
        return categoryRepository.findTopSellingCategoryIds(since, size);
    }

    /**
     * Finds all categories by their IDs.
     *
     * @param ids list of category IDs
     * @return list of matching categories
     */
    @Override
    public List<Category> findAllByIds(List<Long> ids) {
        return categoryRepository.findAllById(ids);
    }
}
