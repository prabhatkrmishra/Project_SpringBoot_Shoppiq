package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.entity.category.Category;

/**
 * <strong>Spring Boot Concept:</strong> Write facade for category persistence.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * A write-only facade that decouples service-layer code from {@code CategoryRepository}.
 * This follows the <strong>CQRS-inspired pattern</strong> — separate interfaces for
 * reads ({@code CategoryLookupService}) and writes ({@code CategoryWriteService}).
 * </p>
 *
 * <h2>Why Separate Read and Write?</h2>
 * <ul>
 *   <li>Clear separation of concerns — one interface for queries, one for commands.</li>
 *   <li>Transactional boundaries can be applied differently for reads vs writes.</li>
 *   <li>Makes the codebase easier to navigate and understand.</li>
 * </ul>
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
