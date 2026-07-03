package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.request.CategoryRequest;
import com.pkmprojects.shoppiq.dto.response.CategoryResponse;

import java.util.List;

/**
 * Service interface for managing product categories.
 *
 * <p>
 * This service defines the business operations for category management.
 * It acts as the boundary between the presentation layer (controllers)
 * and the persistence layer (repositories).
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create new categories.</li>
 *     <li>Bulk create categories.</li>
 *     <li>Update existing categories.</li>
 *     <li>Delete categories.</li>
 *     <li>Retrieve categories by identifier or slug.</li>
 *     <li>Retrieve all available categories.</li>
 *     <li>Enforce business rules such as uniqueness and validation.</li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *     <li>Exposes DTOs instead of JPA entities.</li>
 *     <li>Contains business operations only.</li>
 *     <li>Implementation is responsible for validation and persistence.</li>
 *     <li>Controllers should delegate directly to this interface without
 *     implementing business logic.</li>
 * </ul>
 *
 * <h2>Business Rules</h2>
 * <ul>
 *     <li>Category names must be unique.</li>
 *     <li>Category slugs must be unique.</li>
 *     <li>Slugs are automatically generated from category names.</li>
 *     <li>Categories referenced by products cannot be deleted.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface CategoryService {

    /**
     * Creates a new product category.
     *
     * <p>
     * The implementation performs all required validations before persisting
     * the category.
     * </p>
     *
     * <ul>
     *     <li>Validates request data.</li>
     *     <li>Checks duplicate category names.</li>
     *     <li>Generates a URL-friendly slug.</li>
     *     <li>Ensures slug uniqueness.</li>
     * </ul>
     *
     * @param request category creation request
     * @return newly created category
     */
    CategoryResponse create(CategoryRequest request);

    /**
     * Creates multiple categories at once.
     *
     * @param requests list of category creation requests
     * @return list of newly created categories
     */
    List<CategoryResponse> createBulk(List<CategoryRequest> requests);

    /**
     * Updates an existing category.
     *
     * <p>
     * If the category name changes, a new slug is generated automatically.
     * Uniqueness validation is performed before the update is persisted.
     * </p>
     *
     * @param id      identifier of the category to update
     * @param request updated category data
     * @return updated category
     */
    CategoryResponse update(Long id, CategoryRequest request);

    /**
     * Deletes a category.
     *
     * <p>
     * Implementations should prevent deletion when products still reference
     * the category.
     * </p>
     *
     * @param id identifier of the category
     */
    void delete(Long id);

    /**
     * Retrieves a category by its database identifier.
     *
     * @param id category identifier
     * @return matching category
     */
    CategoryResponse getById(Long id);

    /**
     * Retrieves a category using its URL slug.
     *
     * @param slug URL-friendly category slug
     * @return matching category
     */
    CategoryResponse getBySlug(String slug);

    /**
     * Retrieves all categories.
     *
     * <p>
     * The returned list is typically ordered alphabetically by category name.
     * </p>
     *
     * @return list of all categories
     */
    List<CategoryResponse> getAll();
}