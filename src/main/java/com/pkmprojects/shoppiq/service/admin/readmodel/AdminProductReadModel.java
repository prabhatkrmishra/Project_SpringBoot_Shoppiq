package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.seller.Seller;

import java.util.List;
import java.util.Optional;

/**
 * Read-only product/inventory query facade for admin dashboards and reports.
 *
 * <p>Decouples admin services from {@code ItemRepository},
 * {@code ItemDetailsRepository}, {@code ItemReviewRepository},
 * and {@code SellerRepository}.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
public interface AdminProductReadModel {

    /**
     * Returns total product (item) count.
     */
    long countItems();

    /**
     * Returns total item-details count.
     */
    long countItemDetails();

    /**
     * Returns all item-details records.
     */
    List<ItemDetails> findAllItemDetails();

    /**
     * Returns count of products with stock at or below the threshold (but > 0).
     */
    long countLowStock(int threshold);

    /**
     * Returns count of products with zero stock.
     */
    long countOutOfStock();

    /**
     * Returns the 10 most recently created reviews.
     */
    List<ItemReview> findRecentReviewsTop10();

    /**
     * Finds a seller by ID.
     */
    Optional<Seller> findSellerById(Long sellerId);
}
