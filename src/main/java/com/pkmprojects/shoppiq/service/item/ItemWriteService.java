package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.entity.item.Item;

/**
 * <strong>Spring Boot Concept:</strong> Write facade for item (product) persistence.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * A write-only facade that decouples service-layer code from {@code ItemRepository}.
 * Part of the <strong>CQRS-inspired pattern</strong> where read operations go through
 * {@code ItemLookupService} and write operations through this interface.
 * </p>
 *
 * <h2>Why Separate Read and Write?</h2>
 * <ul>
 *   <li>Clear separation of concerns — query logic stays in {@code ItemLookupService}.</li>
 *   <li>Transactional boundaries can be tuned independently.</li>
 *   <li>Future caching, auditing, or events can be added to writes without affecting reads.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 * @see ItemLookupService
 */
public interface ItemWriteService {

    /**
     * Persists a new or updated item.
     *
     * @param item the item to save
     * @return the saved entity
     */
    Item save(Item item);

    /**
     * Deletes an item.
     *
     * @param item the item to delete
     */
    void delete(Item item);
}
