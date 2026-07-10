package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.admin.AdminInventoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AdminInventoryService}.
 *
 * <p>
 * Provides inventory management operations for administrators
 * including stock viewing, adjustment, and bulk updates.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>List all products with inventory details.</li>
 *     <li>Adjust stock for individual products.</li>
 *     <li>Bulk update stock for multiple products.</li>
 *     <li>Identify low stock and out of stock products.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>Write operations execute in read-write transactions.</li>
 *     <li>Stock adjustments are validated against minimum bounds.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class AdminInventoryServiceImpl implements AdminInventoryService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ItemRepository itemRepository;
    private final ItemDetailsRepository itemDetailsRepository;

    public AdminInventoryServiceImpl(ItemRepository itemRepository,
                                     ItemDetailsRepository itemDetailsRepository) {
        this.itemRepository = itemRepository;
        this.itemDetailsRepository = itemDetailsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminProductInventoryResponse> getAllProductInventory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var itemPage = itemRepository.findAll(pageable);
        return PageResponse.of(itemPage, this::mapToInventoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProductInventoryResponse> getLowStockProducts() {
        List<ItemDetails> lowStock = itemDetailsRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
        return lowStock.stream()
                .map(details -> mapToInventoryResponse(details.getItem()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProductInventoryResponse> getOutOfStockProducts() {
        List<ItemDetails> outOfStock = itemDetailsRepository.findOutOfStockProducts();
        return outOfStock.stream()
                .map(details -> mapToInventoryResponse(details.getItem()))
                .collect(Collectors.toList());
    }

    @Override
    public AdminProductInventoryResponse adjustStock(Long itemId, StockAdjustmentRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        int newQuantity = request.quantity();

        if (newQuantity < 0) {
            throw ItemStockNegativeException.forAdjustment(details.getStockQuantity(), request.quantity());
        }

        details.setStockQuantity(newQuantity);
        itemDetailsRepository.save(details);

        return mapToInventoryResponse(item);
    }

    @Override
    public List<AdminProductInventoryResponse> bulkUpdateStock(java.util.Map<Long, StockAdjustmentRequest> adjustments) {
        List<AdminProductInventoryResponse> results = adjustments.entrySet().stream()
                .map(entry -> {
                    Long itemId = entry.getKey();
                    StockAdjustmentRequest request = entry.getValue();
                    Item item = itemRepository.findById(itemId)
                            .orElseThrow(() -> ItemNotFoundException.id(itemId));
                    ItemDetails details = item.getItemDetails();
                    int newQuantity = request.quantity();

                    if (newQuantity < 0) {
                        throw ItemStockNegativeException.forAdjustment(item.getName(), details.getSku(), details.getStockQuantity(), request.quantity());
                    }

                    details.setStockQuantity(newQuantity);
                    itemDetailsRepository.save(details);
                    return mapToInventoryResponse(item);
                })
                .collect(Collectors.toList());

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDashboardSummary getInventoryDashboardSummary() {
        List<ItemDetails> allDetails = itemDetailsRepository.findAll();
        long totalItems = allDetails.size();
        long outOfStockItems = allDetails.stream()
                .filter(d -> d.getStockQuantity() == 0)
                .count();
        long lowStockItems = allDetails.stream()
                .filter(d -> d.getStockQuantity() > 0 && d.getStockQuantity() <= LOW_STOCK_THRESHOLD)
                .count();
        long inStockItems = totalItems - outOfStockItems - lowStockItems;

        return new InventoryDashboardSummary(
                totalItems,
                inStockItems,
                lowStockItems,
                outOfStockItems
        );
    }

    @Override
    public AdminProductInventoryResponse toggleOnSale(Long itemId, boolean onSale) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        if (onSale && Boolean.TRUE.equals(details.getOnSale())) {
            throw ProductAlreadyOnSaleException.forItem(item.getName());
        }
        details.setOnSale(onSale);
        itemDetailsRepository.save(details);

        return mapToInventoryResponse(item);
    }

    @Override
    public AdminProductInventoryResponse updateDiscount(Long itemId, java.math.BigDecimal discountPercentage) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        details.setDiscountPercentage(discountPercentage);
        itemDetailsRepository.save(details);

        return mapToInventoryResponse(item);
    }

    @Override
    public List<AdminProductInventoryResponse> bulkToggleOnSale(List<Long> itemIds, boolean onSale, java.math.BigDecimal discountPercentage) {
        List<AdminProductInventoryResponse> results = itemIds.stream()
                .map(itemId -> {
                    Item item = itemRepository.findById(itemId)
                            .orElseThrow(() -> ItemNotFoundException.id(itemId));

                    ItemDetails details = item.getItemDetails();
                    if (onSale && Boolean.TRUE.equals(details.getOnSale())) {
                        return mapToInventoryResponse(item);
                    }
                    details.setOnSale(onSale);
                    if (discountPercentage != null) {
                        details.setDiscountPercentage(discountPercentage);
                    }
                    itemDetailsRepository.save(details);

                    return mapToInventoryResponse(item);
                })
                .collect(Collectors.toList());

        return results;
    }

    @Override
    public AdminProductInventoryResponse putOnSale(Long itemId, java.math.BigDecimal discountPercentage) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> ItemNotFoundException.id(itemId));

        ItemDetails details = item.getItemDetails();
        details.setOnSale(true);
        if (discountPercentage != null) {
            details.setDiscountPercentage(discountPercentage);
        }
        itemDetailsRepository.save(details);

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
                LOW_STOCK_THRESHOLD,
                true,
                details.getImageUrl(),
                Boolean.TRUE.equals(details.getOnSale())
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