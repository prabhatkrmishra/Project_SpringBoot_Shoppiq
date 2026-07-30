package com.pkmprojects.shoppiq.dto.admin.response;

import java.math.BigDecimal;

/**
 * Response DTO for the admin product inventory management page.
 *
 * <p>This record provides a product-centric view of inventory with
 * stock levels, pricing information, and computed stock status
 * indicators. It is returned by the admin inventory listing endpoint
 * and is designed for the inventory management UI where administrators
 * monitor stock levels, identify low-stock products, and manage
 * product availability across the catalog.</p>
 *
 * <p>The {@code effectivePrice} is computed server-side as
 * {@code basePrice * (1 - discountPercentage / 100)} and is included
 * for display convenience. The {@code stockStatus} enum provides a
 * normalized indicator for frontend filtering and conditional styling
 * (red for out-of-stock, yellow for low-stock, green for in-stock).</p>
 *
 * @param itemId             unique product identifier
 * @param itemName           product display name
 * @param slug               SEO-friendly URL slug for the product
 * @param description        product description text
 * @param categoryName       name of the product's assigned category
 * @param sku                Stock Keeping Unit identifier for warehouse tracking
 * @param brand              product manufacturer or brand name
 * @param basePrice          base selling price before any discount is applied
 * @param discountPercentage current discount percentage applied to the product
 * @param effectivePrice     computed selling price after discount; calculated
 *                           server-side to ensure accuracy
 * @param stockQuantity      current inventory count for this product
 * @param stockStatus        computed stock indicator (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)
 * @param active             whether the product is currently active and visible in the storefront
 * @param imageUrl           URL of the product's primary image
 * @param onSale             whether the product is currently marked as on sale
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record AdminProductInventoryResponse(

        /**
         * Product identifier.
         */
        Long itemId,

        /**
         * Product name.
         */
        String itemName,

        /**
         * SEO-friendly slug.
         */
        String slug,

        /**
         * Product description.
         */
        String description,

        /**
         * Category name.
         */
        String categoryName,

        /**
         * Product SKU.
         */
        String sku,

        /**
         * Product brand.
         */
        String brand,

        /**
         * Base price before discount.
         */
        BigDecimal basePrice,

        /**
         * Discount percentage.
         */
        BigDecimal discountPercentage,

        /**
         * Effective price after discount.
         */
        BigDecimal effectivePrice,

        /**
         * Current stock quantity.
         */
        int stockQuantity,

        /**
         * Stock status indicator.
         */
        StockStatus stockStatus,

        /**
         * Whether the product is active.
         */
        boolean active,

        /**
         * Product image URL.
         */
        String imageUrl,

        /**
         * Whether the product is currently on sale.
         */
        boolean onSale
) {

    /**
     * Creates an {@code AdminProductInventoryResponse} from entity data.
     *
     * @param itemId             product identifier
     * @param itemName           product name
     * @param description        product description
     * @param categoryName       category name
     * @param sku                product SKU
     * @param brand              product brand
     * @param basePrice          base price
     * @param discountPercentage discount percentage
     * @param stockQuantity      current stock
     * @param lowStockThreshold  low stock threshold
     * @param active             active status
     * @param imageUrl           product image URL
     * @param onSale             whether the product is on sale
     * @return populated response DTO
     */
    public static AdminProductInventoryResponse from(
            Long itemId,
            String itemName,
            String slug,
            String description,
            String categoryName,
            String sku,
            String brand,
            BigDecimal basePrice,
            BigDecimal discountPercentage,
            int stockQuantity,
            int lowStockThreshold,
            boolean active,
            String imageUrl,
            boolean onSale
    ) {
        BigDecimal effectivePrice = discountPercentage != null
                ? basePrice.multiply(
                BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP))
        ).setScale(2, java.math.RoundingMode.HALF_UP)
                : basePrice.setScale(2, java.math.RoundingMode.HALF_UP);

        StockStatus stockStatus;
        if (stockQuantity == 0) {
            stockStatus = StockStatus.OUT_OF_STOCK;
        } else if (stockQuantity <= lowStockThreshold) {
            stockStatus = StockStatus.LOW_STOCK;
        } else {
            stockStatus = StockStatus.IN_STOCK;
        }

        return new AdminProductInventoryResponse(
                itemId,
                itemName,
                slug,
                description,
                categoryName,
                sku,
                brand,
                basePrice,
                discountPercentage,
                effectivePrice,
                stockQuantity,
                stockStatus,
                active,
                imageUrl,
                onSale
        );
    }

    /**
     * Stock status indicator for frontend filtering and conditional styling.
     *
     * <p>Computed server-side by comparing the current stock quantity against
     * the configured low-stock threshold. Used in the inventory management
     * UI to highlight products that need attention.</p>
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
