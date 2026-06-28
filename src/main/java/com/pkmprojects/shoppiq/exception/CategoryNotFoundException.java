package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a requested category cannot be found.
 *
 * <p>
 * This exception represents lookup failures for {@code Category} resources.
 * It is typically thrown by the service layer when a category cannot be
 * resolved using its identifier or URL slug.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represents missing category resources.</li>
 *     <li>Associates the failure with
 *     {@link ErrorCode#CATEGORY_NOT_FOUND}.</li>
 *     <li>Provides expressive factory methods for common lookup failures.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>The constructor is private to enforce factory method usage.</li>
 *     <li>Factory methods centralize message creation.</li>
 *     <li>Additional lookup scenarios can be added without modifying callers.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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