package com.pkmprojects.shoppiq.dto.seller.response;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;

import java.math.BigDecimal;

/**
 * Seller-facing inventory response DTO for product stock management.
 *
 * <p>This record provides inventory information for a product owned by
 * the authenticated seller, including current stock level, computed
 * stock status, pricing, and publishing status. It is returned by the
 * seller inventory listing endpoint and is designed for the inventory
 * management UI where sellers monitor stock levels and manage product
 * availability.</p>
 *
 * <p>The {@code stockStatus} enum provides a normalized indicator for
 * frontend filtering and conditional styling. The status is computed
 * server-side using a low-stock threshold of 5 units: IN_STOCK (above
 * threshold), LOW_STOCK (at or below threshold but above zero), and
 * OUT_OF_STOCK (zero). The static {@link #from(Item)} factory method
 * handles the entity-to-DTO conversion with stock status computation.</p>
 *
 * @param itemId           unique product identifier
 * @param itemName         product display name
 * @param sku              Stock Keeping Unit identifier for warehouse tracking
 * @param brand            product manufacturer or brand name
 * @param basePrice        current selling price of the product
 * @param stockQuantity    current inventory count for this product
 * @param stockStatus      computed stock indicator (IN_STOCK, LOW_STOCK,
 *                         OUT_OF_STOCK); determined by comparing stockQuantity
 *                         against the low-stock threshold of 5 units
 * @param publishingStatus current publishing lifecycle status
 *                         (DRAFT, PUBLISHED, REJECTED) controlling
 *                         storefront visibility
 * @param imageUrl         URL of the product's primary image
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record SellerInventoryResponse(
        /**
         * Product identifier.
         */
        Long itemId,

        /**
         * Product name.
         */
        String itemName,

        /**
         * Stock Keeping Unit.
         */
        String sku,

        /**
         * Product brand.
         */
        String brand,

        /**
         * Current selling price.
         */
        BigDecimal basePrice,

        /**
         * Current inventory count.
         */
        int stockQuantity,

        /**
         * Computed stock status (IN_STOCK, LOW_STOCK, OUT_OF_STOCK).
         */
        StockStatus stockStatus,

        /**
         * Product publishing status (DRAFT, PUBLISHED, REJECTED).
         */
        ProductPublishingStatus publishingStatus,

        /**
         * Product image URL.
         */
        String imageUrl
) {

    private static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Creates a response DTO from the given entity.
     *
     * @param item the item entity
     * @return populated response DTO with computed stock status
     */
    public static SellerInventoryResponse from(Item item) {
        ItemDetails details = item.getItemDetails();

        StockStatus stockStatus;
        int qty = details.getStockQuantity();
        if (qty == 0) {
            stockStatus = StockStatus.OUT_OF_STOCK;
        } else if (qty <= LOW_STOCK_THRESHOLD) {
            stockStatus = StockStatus.LOW_STOCK;
        } else {
            stockStatus = StockStatus.IN_STOCK;
        }

        return new SellerInventoryResponse(
                item.getId(),
                item.getName(),
                details.getSku(),
                details.getBrand(),
                details.getPrice(),
                qty,
                stockStatus,
                item.getPublishingStatus(),
                details.getImageUrl()
        );
    }

    /**
     * Stock status indicator for frontend filtering and conditional styling.
     *
     * <p>Computed server-side by comparing the current stock quantity against
     * the low-stock threshold of 5 units. Used in the seller inventory
     * management UI to highlight products that need restocking attention.</p>
     */
    public enum StockStatus {
        /**
         * In stock (quantity > low stock threshold).
         */
        IN_STOCK,
        /**
         * Low stock (quantity > 0 but <= threshold).
         */
        LOW_STOCK,
        /**
         * Out of stock (quantity = 0).
         */
        OUT_OF_STOCK
    }
}
