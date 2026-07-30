package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.entity.item.Item;

/**
 * Write facade for item (product) persistence.
 *
 * <p>Decouples service-layer code from {@code ItemRepository},
 * providing save and delete operations for item entities.</p>
 *
 * @author prabhatkrmishra
 * @see ItemLookupService
 * @since 1.4.0
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
