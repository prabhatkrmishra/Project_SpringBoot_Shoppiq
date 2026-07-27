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
 * Default implementation of {@link AdminProductReadModel}.
 *
 * <p>Delegates to {@link com.pkmprojects.shoppiq.service.item.ItemLookupService},
 * {@link com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsLookupService},
 * and {@link com.pkmprojects.shoppiq.service.seller.SellerLookupService}
 * for product aggregate queries used in admin dashboards.</p>
 *
 * @author PrabhatKrMishra
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

    @Override
    public long countItems() {
        return itemLookupService.count();
    }

    @Override
    public long countItemDetails() {
        return itemDetailsLookupService.count();
    }

    @Override
    public List<ItemDetails> findAllItemDetails() {
        return itemDetailsLookupService.findAll();
    }

    @Override
    public long countLowStock(int threshold) {
        return itemDetailsLookupService.countLowStockProducts(threshold);
    }

    @Override
    public long countOutOfStock() {
        return itemDetailsLookupService.countOutOfStockProducts();
    }

    @Override
    public List<ItemReview> findRecentReviewsTop10() {
        return itemReviewRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Override
    public Optional<Seller> findSellerById(Long sellerId) {
        return sellerLookupService.findById(sellerId);
    }
}
