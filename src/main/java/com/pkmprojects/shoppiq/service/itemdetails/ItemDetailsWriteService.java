package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;

import java.util.List;

/**
 * Write facade for item-details persistence.
 *
 * <p>Decouples callers from {@code ItemDetailsRepository},
 * providing save and saveAll operations for item-details entities.</p>
 *
 * @author prabhatkrmishra
 * @see ItemDetailsLookupService
 * @since 1.4.0
 */
public interface ItemDetailsWriteService {

    /**
     * Persists new or updated item-details (e.g. stock level change).
     *
     * @param itemDetails the item details to save
     * @return the saved entity
     */
    ItemDetails save(ItemDetails itemDetails);

    /**
     * Persists multiple item-details in a single round-trip.
     *
     * @param itemDetailsList the item details to save
     * @return the saved entities
     */
    List<ItemDetails> saveAll(List<ItemDetails> itemDetailsList);
}
