package com.pkmprojects.shoppiq.exception.general.category;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when attempting to create or update a category whose name already exists.
 *
 * <p>This exception is thrown during category creation or update when the
 * submitted name conflicts with an existing category. It uses the
 * {@link ErrorCode#CATEGORY_ALREADY_EXISTS} code and HTTP 409 Conflict
 * status. Category names are unique within the system to prevent confusion
 * in the product catalog.</p>
 *
 * <p>The detail message includes the conflicting category name (e.g.,
 * "Category with name 'Electronics' already exists.") to help the client
 * understand which field caused the conflict. The client should use a
 * different name or modify the existing category instead.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#CATEGORY_ALREADY_EXISTS
 * @since 1.0.0
 */
public final class DuplicateCategoryException extends DuplicateResourceException {

    /**
     * Creates a duplicate category exception.
     *
     * <p>This constructor is intentionally private to ensure all instances
     * are created through the provided factory methods. This enforces the
     * use of descriptive static factory methods that clearly indicate
     * which field caused the duplication.</p>
     *
     * @param detail detailed description of the duplicate resource
     */
    private DuplicateCategoryException(String detail) {
        super(ErrorCode.CATEGORY_ALREADY_EXISTS, detail);
    }

    /**
     * Creates an exception indicating that the supplied category name
     * already belongs to another category.
     *
     * <p>The detail message includes the conflicting category name
     * (e.g., "Category with name 'Electronics' already exists.") to
     * help the client understand which field caused the conflict.</p>
     *
     * @param category the duplicate category name
     * @return a new exception instance
     */
    public static DuplicateCategoryException category(String category) {
        return new DuplicateCategoryException("Category with name '%s' already exists.".formatted(category));
    }
}
