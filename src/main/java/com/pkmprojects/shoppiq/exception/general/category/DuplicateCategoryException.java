package com.pkmprojects.shoppiq.exception.general.category;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Exception thrown when attempting to
 * create or update a category whose name already exists.
 *
 * <p>Leaf exception in the duplicate-resource hierarchy. Extends
 * {@link com.pkmprojects.shoppiq.exception.business.DuplicateResourceException}
 * (HTTP 409) for category name uniqueness violations.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public final class DuplicateCategoryException extends DuplicateResourceException {

    /**
     * Creates a duplicate category exception.
     *
     * <p>
     * This constructor is intentionally private to ensure all instances are
     * created through the provided factory methods.
     * </p>
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
     * @param category duplicate category name
     * @return duplicate category exception
     */
    /**
     * Creates an exception for a duplicate category name.
     *
     * @param category the duplicate category name
     * @return a new exception instance
     */
    public static DuplicateCategoryException category(String category) {
        return new DuplicateCategoryException("Category with name '%s' already exists.".formatted(category));
    }
}
