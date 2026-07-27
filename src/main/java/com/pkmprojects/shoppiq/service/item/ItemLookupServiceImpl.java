package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.repository.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ItemLookupServiceImpl implements ItemLookupService {

    private final ItemRepository itemRepository;

    @Override
    public Optional<Item> findById(Long itemId) {
        return itemRepository.findById(itemId);
    }

    @Override
    public Optional<Item> findBySlug(String slug) {
        return itemRepository.findBySlug(slug);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return itemRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByItemDetailsSku(String sku) {
        return itemRepository.existsByItemDetailsSku(sku);
    }

    @Override
    public boolean existsByItemDetailsSkuAndIdNot(String sku, Long id) {
        return itemRepository.existsByItemDetailsSkuAndIdNot(sku, id);
    }

    @Override
    public long count() {
        return itemRepository.count();
    }

    @Override
    public long countBySellerId(Long sellerId) {
        return itemRepository.countBySellerId(sellerId);
    }

    @Override
    public Page<Item> findAll(int page, int size) {
        return itemRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Page<Item> findBySellerId(Long sellerId, int page, int size) {
        return itemRepository.findBySellerId(sellerId,
                PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @Override
    public Page<Item> findByPublishingStatus(ProductPublishingStatus status, int page, int size) {
        return itemRepository.findByPublishingStatus(status,
                PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @Override
    public Optional<Item> findByIdAndSellerId(Long id, Long sellerId) {
        return itemRepository.findByIdAndSellerId(id, sellerId);
    }

    @Override
    public List<Item> findByNameContaining(String name, int page, int size) {
        return itemRepository.findByNameContainingIgnoreCase(name,
                PageRequest.of(page, size));
    }

    @Override
    public List<Item> findNewArrivals(ProductPublishingStatus status, int page, int size) {
        return itemRepository.findNewArrivals(status,
                PageRequest.of(page, size));
    }

    @Override
    public Page<Item> findNewArrivalsPage(ProductPublishingStatus status, int page, int size) {
        return itemRepository.findNewArrivalsPage(status,
                PageRequest.of(page, size));
    }

    @Override
    public Page<Item> findOnSaleItemsPage(int page, int size) {
        return itemRepository.findOnSaleItemsPage(PageRequest.of(page, size));
    }

    @Override
    public Page<Item> findByCategorySlug(String slug, int page, int size) {
        return itemRepository.findByCategorySlug(slug, PageRequest.of(page, size));
    }

    @Override
    public List<Object[]> findTopSellingItemIds(Instant since, int size) {
        return itemRepository.findTopSellingItemIds(since, size);
    }

    @Override
    public Set<String> findExistingSkus(Set<String> skus) {
        return itemRepository.findExistingSkus(skus);
    }

    @Override
    public List<Item> findAllByIds(List<Long> ids) {
        return itemRepository.findAllById(ids);
    }

    @Override
    public Page<Item> findAllWithItemDetails(int page, int size) {
        return itemRepository.findAllWithItemDetails(PageRequest.of(page, size));
    }
}
