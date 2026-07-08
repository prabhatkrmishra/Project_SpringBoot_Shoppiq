package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.Category;

import java.math.BigDecimal;

/**
 * Response DTO for admin product inventory listing.
 *
 * <p>
 * This DTO provides a product-centric view of inventory with
 * stock levels, pricing, and low-stock indicators for the
 * administrator inventory management page.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose product inventory data to the admin API.</li>
 *     <li>Include computed fields like stock status and effective price.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Includes stock status enum for easy frontend filtering.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
        String imageUrl
) {

    /**
     * Stock status enum for easy frontend filtering.
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
     * @return populated response DTO
     */
    public static AdminProductInventoryResponse from(
            Long itemId,
            String itemName,
            String description,
            String categoryName,
            String sku,
            String brand,
            BigDecimal basePrice,
            BigDecimal discountPercentage,
            int stockQuantity,
            int lowStockThreshold,
            boolean active,
            String imageUrl
    ) {
        BigDecimal effectivePrice = basePrice.multiply(
                BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP))
        ).setScale(2, java.math.RoundingMode.HALF_UP);

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
                imageUrl
        );
    }
}