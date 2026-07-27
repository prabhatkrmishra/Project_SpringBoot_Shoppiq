package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.response.AdminProductResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.item.ItemWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminProductServiceImpl implements AdminProductService {

    private final ItemLookupService itemLookupService;
    private final ItemWriteService itemWriteService;

    public AdminProductServiceImpl(ItemLookupService itemLookupService,
                                   ItemWriteService itemWriteService) {
        this.itemLookupService = itemLookupService;
        this.itemWriteService = itemWriteService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminProductResponse> getPendingProducts(int page, int size) {
        var itemPage = itemLookupService.findByPublishingStatus(ProductPublishingStatus.DRAFT, page, size);
        return PageResponse.of(itemPage, AdminProductResponse::from);
    }

    @Override
    public AdminProductResponse publishProduct(Long itemId) {
        Item item = findItem(itemId);
        item.setPublishingStatus(ProductPublishingStatus.PUBLISHED);
        itemWriteService.save(item);
        return AdminProductResponse.from(item);
    }

    @Override
    public AdminProductResponse rejectProduct(Long itemId) {
        Item item = findItem(itemId);
        item.setPublishingStatus(ProductPublishingStatus.REJECTED);
        itemWriteService.save(item);
        return AdminProductResponse.from(item);
    }

    private Item findItem(Long itemId) {
        return itemLookupService.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));
    }
}
