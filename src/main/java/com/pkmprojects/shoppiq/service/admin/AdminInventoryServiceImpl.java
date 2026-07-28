package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.exception.general.inventory.ItemStockNegativeException;
import com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.general.item.ProductAlreadyOnSaleException;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsLookupService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsWriteService;
import com.pkmprojects.shoppiq.config.InventoryConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link AdminInventoryService}
 * containing business logic for admin inventory management.
 *
 * <p>Provides paginated inventory listing, stock adjustment, bulk stock updates,
 * low/out-of-stock identification, and on-sale/discount management. Used by
 * {@code AdminInventoryController}.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Stock adjustments and bulk updates are atomic; reads use {@code readOnly = true}.</li>
 *   <li><strong>Constructor injection</strong> — final fields for immutability and testability.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see AdminInventoryService
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminInventoryServiceImpl implements AdminInventoryService {

    private final ItemLookupService itemLookupService;
    private final ItemDetailsLookupService itemDetailsLookupService;
    private final ItemDetailsWriteService itemDetailsWriteService;

    public AdminInventoryServiceImpl(ItemLookupService itemLookupService,
                                     ItemDetailsLookupService itemDetailsLookupService,
                                     ItemDetailsWriteService itemDetailsWriteService) {
        this.itemLookupService = itemLookupService;
        this.itemDetailsLookupService = itemDetailsLookupService;
        this.itemDetailsWriteService = itemDetailsWriteService;
    }

    /**
     * Retrieves a paginated list of all products with inventory details.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated product inventory responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminProductInventoryResponse> getAllProductInventory(int page, int size) {
        var itemPage = itemLookupService.findAll(page, size);
        return PageResponse.of(itemPage, this::mapToInventoryResponse);
    }

    /**
     * Retrieves products with stock below the low-stock threshold.
     *
     * @return list of low-stock product inventory responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminProductInventoryResponse> getLowStockProducts() {
        List<ItemDetails> lowStock = itemDetailsLookupService.findLowStockProducts(InventoryConstants.LOW_STOCK_THRESHOLD);
        return lowStock.stream()
                .map(details -> mapToInventoryResponse(details.getItem()))
                .toList();
    }

    /**
     * Retrieves products with zero stock.
     *
     * @return list of out-of-stock product inventory responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminProductInventoryResponse> getOutOfStockProducts() {
        List<ItemDetails> outOfStock = itemDetailsLookupService.findOutOfStockProducts();
        return outOfStock.stream()
                .map(details -> mapToInventoryResponse(details.getItem()))
                .toList();
    }

    /**
     * Adjusts the stock quantity for a product.
     *
     * <p>Validates that the new quantity is non-negative before persisting.</p>
     *
     * @param itemId  item ID
     * @param request stock adjustment payload containing new quantity
     * @return updated product inventory response
     * @throws ItemNotFoundException          if the item does not exist
     * @throws ItemStockNegativeException     if the new quantity is negative
     */
    @Override
    public AdminProductInventoryResponse adjustStock(Long itemId, StockAdjustmentRequest request) {
        Item item = itemLookupService.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        int newQuantity = request.quantity();

        if (newQuantity < 0) {
            throw ItemStockNegativeException.forAdjustment(details.getStockQuantity(), request.quantity());
        }

        details.setStockQuantity(newQuantity);
        itemDetailsWriteService.save(details);

        return mapToInventoryResponse(item);
    }

    /**
     * Updates stock for multiple products in a single transaction.
     *
     * @param adjustments map of item ID to stock adjustment request
     * @return list of updated product inventory responses
     */
    @Override
    public List<AdminProductInventoryResponse> bulkUpdateStock(java.util.Map<Long, StockAdjustmentRequest> adjustments) {
        List<AdminProductInventoryResponse> results = adjustments.entrySet().stream()
                .map(entry -> {
                    Long itemId = entry.getKey();
                    StockAdjustmentRequest request = entry.getValue();
                    Item item = itemLookupService.findById(itemId)
                            .orElseThrow(() -> ItemNotFoundException.id(itemId));
                    ItemDetails details = item.getItemDetails();
                    int newQuantity = request.quantity();

                    if (newQuantity < 0) {
                        throw ItemStockNegativeException.forAdjustment(item.getName(), details.getSku(), details.getStockQuantity(), request.quantity());
                    }

                    details.setStockQuantity(newQuantity);
                    itemDetailsWriteService.save(details);
                    return mapToInventoryResponse(item);
                })
                .toList();

        return results;
    }

    /**
     * Computes inventory dashboard summary with total, in-stock, low-stock, and out-of-stock counts.
     *
     * @return inventory dashboard summary
     */
    @Override
    @Transactional(readOnly = true)
    public InventoryDashboardSummary getInventoryDashboardSummary() {
        long totalItems = itemDetailsLookupService.count();
        long outOfStockItems = itemDetailsLookupService.countOutOfStockProducts();
        long lowStockItems = itemDetailsLookupService.countLowStockProducts(InventoryConstants.LOW_STOCK_THRESHOLD);
        long inStockItems = totalItems - outOfStockItems - lowStockItems;

        return new InventoryDashboardSummary(
                totalItems,
                inStockItems,
                lowStockItems,
                outOfStockItems
        );
    }

    /**
     * Toggles the on-sale status for a product.
     *
     * @param itemId item ID
     * @param onSale desired on-sale state
     * @return updated product inventory response
     * @throws ItemNotFoundException               if the item does not exist
     * @throws ProductAlreadyOnSaleException        if the product is already on sale
     */
    @Override
    public AdminProductInventoryResponse toggleOnSale(Long itemId, boolean onSale) {
        Item item = itemLookupService.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        if (onSale && Boolean.TRUE.equals(details.isOnSale())) {
            throw ProductAlreadyOnSaleException.forItem(item.getName());
        }
        details.setOnSale(onSale);
        itemDetailsWriteService.save(details);

        return mapToInventoryResponse(item);
    }

    /**
     * Updates the discount percentage for a product.
     *
     * @param itemId            item ID
     * @param discountPercentage new discount percentage
     * @return updated product inventory response
     * @throws ItemNotFoundException if the item does not exist
     */
    @Override
    public AdminProductInventoryResponse updateDiscount(Long itemId, java.math.BigDecimal discountPercentage) {
        Item item = itemLookupService.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        details.setDiscountPercentage(discountPercentage);
        itemDetailsWriteService.save(details);

        return mapToInventoryResponse(item);
    }

    /**
     * Toggles on-sale status and optionally updates discount for multiple products.
     *
     * @param itemIds            list of item IDs
     * @param onSale             desired on-sale state
     * @param discountPercentage optional discount percentage to apply
     * @return list of updated product inventory responses
     */
    @Override
    public List<AdminProductInventoryResponse> bulkToggleOnSale(List<Long> itemIds, boolean onSale, java.math.BigDecimal discountPercentage) {
        List<AdminProductInventoryResponse> results = itemIds.stream()
                .map(itemId -> {
                    Item item = itemLookupService.findById(itemId)
                            .orElseThrow(() -> ItemNotFoundException.id(itemId));

                    ItemDetails details = item.getItemDetails();
                    if (onSale && Boolean.TRUE.equals(details.isOnSale())) {
                        return mapToInventoryResponse(item);
                    }
                    details.setOnSale(onSale);
                    if (discountPercentage != null) {
                        details.setDiscountPercentage(discountPercentage);
                    }
                    itemDetailsWriteService.save(details);

                    return mapToInventoryResponse(item);
                })
                .toList();

        return results;
    }

    /**
     * Puts a product on sale with an optional discount percentage.
     *
     * @param itemId             item ID
     * @param discountPercentage optional discount percentage
     * @return updated product inventory response
     * @throws ItemNotFoundException if the item does not exist
     */
    @Override
    public AdminProductInventoryResponse putOnSale(Long itemId, java.math.BigDecimal discountPercentage) {
        Item item = itemLookupService.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        details.setOnSale(true);
        if (discountPercentage != null) {
            details.setDiscountPercentage(discountPercentage);
        }
        itemDetailsWriteService.save(details);

        return mapToInventoryResponse(item);
    }

    private AdminProductInventoryResponse mapToInventoryResponse(Item item) {
        ItemDetails details = item.getItemDetails();
        return AdminProductInventoryResponse.from(
                item.getId(),
                item.getName(),
                item.getSlug(),
                item.getDescription(),
                details.getCategory().getName(),
                details.getSku(),
                details.getBrand(),
                details.getPrice(),
                details.getDiscountPercentage(),
                details.getStockQuantity(),
                InventoryConstants.LOW_STOCK_THRESHOLD,
                true,
                details.getImageUrl(),
                Boolean.TRUE.equals(details.isOnSale())
        );
    }

    /**
     * Bulk stock adjustment record.
     */
    public record BulkStockAdjustment(
            Long itemId,
            int quantity,
            String reason
    ) {
    }
}
