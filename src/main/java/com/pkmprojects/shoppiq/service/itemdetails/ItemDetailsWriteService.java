package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Write facade for item-details persistence.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * A write-only facade decoupling callers from {@code ItemDetailsRepository}.
 * Part of the CQRS-inspired pattern — read queries go through
 * {@code ItemDetailsLookupService}, mutations go through this interface.
 * </p>
 *
 * <h2>Consumers</h2>
 * <ul>
 *     <li>{@code InventoryService} — reduces/restores stock during
 *         checkout and order cancellations.</li>
 *     <li>{@code AdminInventoryServiceImpl} — admin stock adjustments.</li>
 *     <li>{@code SellerInventoryServiceImpl} — seller stock management.</li>
 * </ul>
 *
 * @author prabhatkrmishra
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

    /**
     * Persists multiple item-details in a single round-trip.
     *
     * @param itemDetailsList the item details to save
     * @return the saved entities
     */
    List<ItemDetails> saveAll(List<ItemDetails> itemDetailsList);
}
