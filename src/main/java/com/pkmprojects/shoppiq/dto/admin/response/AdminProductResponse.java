package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;

import java.math.BigDecimal;

/**
 * Admin-facing product response DTO for product management.
 *
 * <p>This record provides a comprehensive view of a product for
 * administrators, including seller attribution and publishing status.
 * It is returned by the admin product list and detail endpoints and
 * is designed for the product management UI where administrators
 * review, approve, and manage the product catalog across all sellers.</p>
 *
 * <p>The static {@link #from(Item)} factory method traverses the
 * {@code Item → ItemDetails → Category} entity graph to flatten
 * the data into a single DTO. Seller information is included to
 * enable administrators to filter products by seller and manage
 * seller-specific catalog concerns.</p>
 *
 * @param itemId           unique product identifier
 * @param name             product display name
 * @param description      product description text
 * @param sku              Stock Keeping Unit identifier for warehouse tracking
 * @param brand            product manufacturer or brand name
 * @param price            current selling price of the product
 * @param stockQuantity    current inventory count; nullable if stock
 *                         information is not available
 * @param categoryName     name of the product's assigned category
 * @param sellerId         identifier of the seller who owns this product;
 *                         nullable if the product has no assigned seller
 * @param sellerName       business name of the owning seller; nullable
 *                         if the seller entity has been removed
 * @param publishingStatus current publishing lifecycle status
 *                         (DRAFT, PUBLISHED, REJECTED) controlling
 *                         storefront visibility
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminProductResponse(
        /**
         * Product identifier.
         */
        Long itemId,

        /**
         * Product name.
         */
        String name,

        /**
         * Product description.
         */
        String description,

        /**
         * Stock Keeping Unit.
         */
        String sku,

        /**
         * Product brand.
         */
        String brand,

        /**
         * Product price.
         */
        BigDecimal price,

        /**
         * Current stock quantity.
         */
        Integer stockQuantity,

        /**
         * Product category name.
         */
        String categoryName,

        /**
         * Seller identifier.
         */
        Long sellerId,

        /**
         * Seller business name.
         */
        String sellerName,

        /**
         * Current publishing status.
         */
        ProductPublishingStatus publishingStatus
) {
    /**
     * Creates a response DTO from the given entity.
     *
     * @param item the item entity
     * @return populated response DTO
     */
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
