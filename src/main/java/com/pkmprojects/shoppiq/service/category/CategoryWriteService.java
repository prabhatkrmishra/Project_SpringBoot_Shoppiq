package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.entity.category.Category;

/**
 * Write facade for category persistence.
 *
 * <p>Decouples service-layer code from {@code CategoryRepository},
 * providing save and delete operations for category entities.</p>
 *
 * @author prabhatkrmishra
 * @see CategoryLookupService
 * @since 1.4.0
 */
public interface CategoryWriteService {

    /**
     * Persists a new or updated category.
     *
     * @param category the category to save
     * @return the saved entity
     */
    Category save(Category category);

    /**
     * Deletes a category.
     *
     * @param category the category to delete
     */
    void delete(Category category);
}
