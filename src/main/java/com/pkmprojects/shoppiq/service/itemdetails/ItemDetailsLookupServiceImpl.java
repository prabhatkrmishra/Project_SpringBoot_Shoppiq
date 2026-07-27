package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.repository.item.ItemDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link ItemDetailsLookupService}.
 *
 * <p>All queries run in a read-only transaction. Delegates directly
 * to {@code ItemDetailsRepository}.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ItemDetailsLookupServiceImpl implements ItemDetailsLookupService {

    private final ItemDetailsRepository itemDetailsRepository;

    @Override
    public Optional<ItemDetails> findById(Long id) {
        return itemDetailsRepository.findById(id);
    }

    @Override
    public long count() {
        return itemDetailsRepository.count();
    }

    @Override
    public List<ItemDetails> findLowStockProducts(int threshold) {
        return itemDetailsRepository.findLowStockProducts(threshold);
    }

    @Override
    public List<ItemDetails> findOutOfStockProducts() {
        return itemDetailsRepository.findOutOfStockProducts();
    }

    @Override
    public long countLowStockProducts(int threshold) {
        return itemDetailsRepository.countLowStockProducts(threshold);
    }

    @Override
    public long countOutOfStockProducts() {
        return itemDetailsRepository.countOutOfStockProducts();
    }

    @Override
    public List<ItemDetails> findLowStockProductsBySellerId(int threshold, Long sellerId) {
        return itemDetailsRepository.findLowStockProductsBySellerId(threshold, sellerId);
    }

    @Override
    public List<ItemDetails> findOutOfStockProductsBySellerId(Long sellerId) {
        return itemDetailsRepository.findOutOfStockProductsBySellerId(sellerId);
    }

    @Override
    public List<ItemDetails> findAll() {
        return itemDetailsRepository.findAll();
    }
}
