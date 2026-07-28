package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.repository.item.ItemReviewRepository;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsLookupService;
import com.pkmprojects.shoppiq.service.seller.SellerLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link AdminProductReadModel}
 * providing read-only product queries for admin dashboards and reports.
 *
 * <p>Delegates to {@link ItemLookupService}, {@link ItemDetailsLookupService},
 * {@code ItemReviewRepository}, and {@link SellerLookupService} for product
 * aggregate queries.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional(readOnly = true)</strong> — All queries are read-only, optimized for database performance.</li>
 *   <li><strong>@RequiredArgsConstructor</strong> — Lombok-generated constructor injection for final fields.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see AdminProductReadModel
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AdminProductReadModelImpl implements AdminProductReadModel {

    private final ItemLookupService itemLookupService;
    private final ItemDetailsLookupService itemDetailsLookupService;
    private final ItemReviewRepository itemReviewRepository;
    private final SellerLookupService sellerLookupService;

    /**
     * Returns the total number of items.
     *
     * @return total item count
     */
    @Override
    public long countItems() {
        return itemLookupService.count();
    }

    /**
     * Returns the total number of item details records.
     *
     * @return total item details count
     */
    @Override
    public long countItemDetails() {
        return itemDetailsLookupService.count();
    }

    /**
     * Returns all item details records.
     *
     * @return list of all item details
     */
    @Override
    public List<ItemDetails> findAllItemDetails() {
        return itemDetailsLookupService.findAll();
    }

    /**
     * Counts item details with stock below the specified threshold.
     *
     * @param threshold the low-stock threshold
     * @return count of low-stock products
     */
    @Override
    public long countLowStock(int threshold) {
        return itemDetailsLookupService.countLowStockProducts(threshold);
    }

    /**
     * Counts item details with zero stock.
     *
     * @return count of out-of-stock products
     */
    @Override
    public long countOutOfStock() {
        return itemDetailsLookupService.countOutOfStockProducts();
    }

    /**
     * Retrieves the 10 most recently created reviews.
     *
     * @return list of the 10 most recent reviews
     */
    @Override
    public List<ItemReview> findRecentReviewsTop10() {
        return itemReviewRepository.findTop10ByOrderByCreatedAtDesc();
    }

    /**
     * Finds a seller by ID.
     *
     * @param sellerId the seller ID
     * @return optional containing the seller if found
     */
    @Override
    public Optional<Seller> findSellerById(Long sellerId) {
        return sellerLookupService.findById(sellerId);
    }
}
