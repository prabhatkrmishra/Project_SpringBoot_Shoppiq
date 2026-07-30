package com.pkmprojects.shoppiq.exception.general.category;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested category cannot be found by ID, slug, or bulk identifier.
 *
 * <p>This exception is thrown by category service methods when a database
 * lookup for a category fails. It uses the {@link ErrorCode#CATEGORY_NOT_FOUND}
 * code and HTTP 404 Not Found status. The exception provides multiple
 * static factory methods to create instances for different lookup
 * scenarios, each with a descriptive detail message.</p>
 *
 * <p>The detail message includes the lookup identifier and type (e.g.,
 * "Category with id '5' was not found.") to help clients understand which
 * identifier was invalid. The client should verify the identifier and
 * retry the request. For bulk operations, a generic message is returned
 * indicating that one or more identifiers were invalid.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#CATEGORY_NOT_FOUND
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
