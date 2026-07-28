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

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link AdminProductService}
 * containing business logic for admin product publishing workflow.
 *
 * <p>Lists products pending review and transitions publishing status between
 * DRAFT, PUBLISHED, and REJECTED. Used by {@code AdminProductController}.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Publishing status transitions are atomic; reads use {@code readOnly = true}.</li>
 *   <li><strong>Constructor injection</strong> — final fields for immutability and testability.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see AdminProductService
 * @since 1.0.0
 */
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

    /**
     * Retrieves a paginated list of products with DRAFT publishing status for admin review.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated admin product responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminProductResponse> getPendingProducts(int page, int size) {
        var itemPage = itemLookupService.findByPublishingStatus(ProductPublishingStatus.DRAFT, page, size);
        return PageResponse.of(itemPage, AdminProductResponse::from);
    }

    /**
     * Publishes a product — transitions DRAFT to PUBLISHED, making it visible to customers.
     *
     * @param itemId item ID
     * @return updated admin product response
     * @throws ItemNotFoundException if the item does not exist
     */
    @Override
    public AdminProductResponse publishProduct(Long itemId) {
        Item item = findItem(itemId);
        item.setPublishingStatus(ProductPublishingStatus.PUBLISHED);
        itemWriteService.save(item);
        return AdminProductResponse.from(item);
    }

    /**
     * Rejects a product — transitions DRAFT to REJECTED, hiding it from the catalog.
     *
     * @param itemId item ID
     * @return updated admin product response
     * @throws ItemNotFoundException if the item does not exist
     */
    @Override
    public AdminProductResponse rejectProduct(Long itemId) {
        Item item = findItem(itemId);
        item.setPublishingStatus(ProductPublishingStatus.REJECTED);
        itemWriteService.save(item);
        return AdminProductResponse.from(item);
    }

    /**
     * Finds an item by ID or throws {@link ItemNotFoundException}.
     */
    private Item findItem(Long itemId) {
        return itemLookupService.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));
    }
}
