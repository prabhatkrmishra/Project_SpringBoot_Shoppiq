package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;

import java.math.BigDecimal;

/**
 * Admin-facing product response DTO.
 *
 * <p>Provides product information including seller details and
 * publishing status for admin product management.</p>
 *
 * @param itemId           product identifier
 * @param name             product name
 * @param description      product description
 * @param sku              stock keeping unit
 * @param brand            product brand
 * @param price            product price
 * @param stockQuantity    current stock quantity
 * @param categoryName     product category name
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
        String description,
        String sku,
        String brand,
        BigDecimal price,
        Integer stockQuantity,
        String categoryName,
        Long sellerId,
        String sellerName,
        ProductPublishingStatus publishingStatus
) {
    public static AdminProductResponse from(Item item) {
        return new AdminProductResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getItemDetails().getSku(),
                item.getItemDetails().getBrand(),
                item.getItemDetails().getPrice(),
                item.getItemDetails().getStockQuantity(),
                item.getItemDetails().getCategory() != null
                        ? item.getItemDetails().getCategory().getName() : null,
                item.getSeller() != null ? item.getSeller().getId() : null,
                item.getSeller() != null ? item.getSeller().getBusinessName() : null,
                item.getPublishingStatus()
        );
    }
}
