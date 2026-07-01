package com.pkmprojects.shoppiq.dto.seller.response;

import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemDetails;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;

import java.math.BigDecimal;

/**
 * Seller-facing inventory response DTO.
 *
 * <p>Provides inventory information for a product owned by a seller,
 * including current stock level, computed stock status, and publishing
 * status.</p>
 *
 * @param itemId           product identifier
 * @param itemName         product name
 * @param sku              stock keeping unit
 * @param brand            product brand
 * @param basePrice        current selling price
 * @param stockQuantity    current inventory count
 * @param stockStatus      computed stock status (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)
 * @param publishingStatus product publishing status (DRAFT, PUBLISHED, REJECTED)
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record SellerInventoryResponse(
        Long itemId,
        String itemName,
        String sku,
        String brand,
        BigDecimal basePrice,
        int stockQuantity,
        StockStatus stockStatus,
        ProductPublishingStatus publishingStatus
) {

    public enum StockStatus {
        IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    }

    private static final int LOW_STOCK_THRESHOLD = 5;

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
                item.getPublishingStatus()
        );
    }
}
