package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.admin.response.AdminProductResponse;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.service.admin.AdminProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of {@link AdminProductService}.
 *
 * <p>
 * Manages product lifecycle operations for admin users, including
 * reviewing and publishing seller products.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminProductServiceImpl implements AdminProductService {

    private final ItemRepository itemRepository;

    public AdminProductServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProductResponse> getPendingProducts() {
        return itemRepository.findAll().stream()
                .filter(item -> item.getPublishingStatus() == ProductPublishingStatus.DRAFT)
                .map(AdminProductResponse::from)
                .toList();
    }

    @Override
    public AdminProductResponse publishProduct(Long itemId) {
        Item item = findItem(itemId);
        item.setPublishingStatus(ProductPublishingStatus.PUBLISHED);
        itemRepository.save(item);
        return AdminProductResponse.from(item);
    }

    @Override
    public AdminProductResponse rejectProduct(Long itemId) {
        Item item = findItem(itemId);
        item.setPublishingStatus(ProductPublishingStatus.REJECTED);
        itemRepository.save(item);
        return AdminProductResponse.from(item);
    }

    private Item findItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));
    }
}
