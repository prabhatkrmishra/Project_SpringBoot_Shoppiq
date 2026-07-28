package com.pkmprojects.shoppiq.exception.general.category;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when a requested
 * category cannot be found.
 *
 * <p>Leaf exception in the resource-not-found hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException}
 * (HTTP 404) for missing categories, with factory methods for lookup by
 * identifier, slug, and bulk operations.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class CategoryNotFoundException extends ResourceNotFoundException {

    /**
     * Creates a category not found exception.
     *
     * @param detail detailed error description
     */
    private CategoryNotFoundException(String detail) {
        super(ErrorCode.CATEGORY_NOT_FOUND, detail);
    }

    /**
     * Creates an exception indicating that no category exists with the
     * supplied identifier.
     *
     * @param id category identifier
     * @return category not found exception
     */
    public static CategoryNotFoundException id(Long id) {
        return new CategoryNotFoundException(
                "Category with id '%d' was not found.".formatted(id)
        );
    }

    /**
     * Creates an exception indicating that no category exists with the
     * supplied slug.
     *
     * @param slug category slug
     * @return category not found exception
     */
    public static CategoryNotFoundException slug(String slug) {
        return new CategoryNotFoundException(
                "Category with slug '%s' was not found.".formatted(slug)
        );
    }

    /**
     * Creates an exception indicating that one or more category identifiers
     * supplied during a bulk operation do not exist.
     *
     * @return category not found exception
     */
    public static CategoryNotFoundException ids() {
        return new CategoryNotFoundException(
                "One or more category identifiers were not found."
        );
    }
}
