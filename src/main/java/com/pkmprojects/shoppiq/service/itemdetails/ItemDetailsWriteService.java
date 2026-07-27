package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;

/**
 * Write facade for item-details persistence.
 *
 * <p>Decouples caller code from {@code ItemDetailsRepository},
 * providing mutation operations for inventory management.</p>
 *
 * <h2>Consumers</h2>
 * <ul>
 *     <li>{@code InventoryService} — reduces/restores stock during
 *         checkout and order cancellations.</li>
 *     <li>{@code AdminInventoryServiceImpl} — admin stock adjustments.</li>
 *     <li>{@code SellerInventoryServiceImpl} — seller stock management.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 * @see ItemDetailsLookupService
 */
public interface ItemDetailsWriteService {

    /**
     * Persists new or updated item-details (e.g. stock level change).
     *
     * @param itemDetails the item details to save
     * @return the saved entity
     */
    ItemDetails save(ItemDetails itemDetails);
}
