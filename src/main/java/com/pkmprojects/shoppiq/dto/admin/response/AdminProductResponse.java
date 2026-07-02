package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;

/**
 * Admin-facing product response DTO.
 *
 * <p>Provides product information including seller details and
 * publishing status for admin product management.</p>
 *
 * @param itemId           product identifier
 * @param name             product name
 * @param sku              stock keeping unit
 * @param sellerId         seller identifier
 * @param sellerName       seller business name
 * @param publishingStatus current publishing status
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record AdminProductResponse(
        Long itemId,
        String name,
        String sku,
        Long sellerId,
        String sellerName,
        ProductPublishingStatus publishingStatus
) {
    public static AdminProductResponse from(Item item) {
        return new AdminProductResponse(
                item.getId(),
                item.getName(),
                item.getItemDetails().getSku(),
                item.getSeller() != null ? item.getSeller().getId() : null,
                item.getSeller() != null ? item.getSeller().getBusinessName() : null,
                item.getPublishingStatus()
        );
    }
}
