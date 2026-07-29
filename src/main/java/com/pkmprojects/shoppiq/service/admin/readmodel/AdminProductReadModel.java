package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.seller.Seller;

import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Read-only product/inventory query facade for admin dashboards and reports.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * A ReadModel facade that decouples admin services from {@code ItemRepository},
 * {@code ItemDetailsRepository}, and {@code SellerRepository}. Provides aggregate
 * queries over product, inventory, and seller data.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Count items, item-details, low-stock, and out-of-stock products.</li>
 *   <li>Retrieve all item-details for inventory reports.</li>
 *   <li>Query recent reviews for activity feeds.</li>
 *   <li>Lookup seller by ID for commission reports.</li>
 * </ul>
 *
 * <p>Decouples admin services from {@code ItemRepository},
 * {@code ItemDetailsRepository}, {@code ItemReviewRepository},
 * and {@code SellerRepository}.</p>
 *
 * @author prabhatkrmishra
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

    /**
     * Returns the total stock quantity across all item details.
     *
     * @return sum of all stock quantities
     */
    long sumStockQuantity();

    /**
     * Returns the total inventory value (price * stock quantity) across all item details.
     *
     * @return total inventory value
     */
    java.math.BigDecimal sumInventoryValue();
}
