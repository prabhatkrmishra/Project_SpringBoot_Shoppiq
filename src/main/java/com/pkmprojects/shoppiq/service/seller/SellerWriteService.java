package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.entity.seller.Seller;

/**
 * Write facade for seller persistence.
 *
 * <p>Decouples caller code from {@code SellerRepository},
 * providing save and deleteById operations for seller entities.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface SellerWriteService {

    /**
     * Persists a new or updated seller.
     */
    Seller save(Seller seller);

    /**
     * Deletes a seller by primary key.
     */
    void deleteById(Long sellerId);
}
