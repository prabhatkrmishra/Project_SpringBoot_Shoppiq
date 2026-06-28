package com.pkmprojects.shoppiq.exception;

import com.pkmprojects.shoppiq.exception.business.DuplicateResourceException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when attempting to create or update a category whose
 * name already exists.
 *
 * <p>
 * Categories are uniquely identified by both their human-readable name
 * and generated slug. This exception is raised whenever a duplicate
 * category name would violate business rules.
 * </p>
 *
 * <h2>Typical Scenarios</h2>
 * <ul>
 *     <li>Creating a category with an existing name.</li>
 *     <li>Renaming a category to a name already used by another category.</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>
 * Category "Electronics" already exists.
 * </pre>
 *
 * @author PrabhatKrMishra
 * @see CategoryNotFoundException
 * @since 1.0.0
 */
public class DuplicateCategoryException extends DuplicateResourceException {

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
    public static DuplicateCategoryException category(String category) {
        return new DuplicateCategoryException("Category with name '%s' already exists.".formatted(category));
    }
}