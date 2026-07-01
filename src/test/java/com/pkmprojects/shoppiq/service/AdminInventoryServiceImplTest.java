package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.admin.AdminInventoryService;
import com.pkmprojects.shoppiq.service.impl.AdminInventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminInventoryServiceImpl Tests")
class AdminInventoryServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemDetailsRepository itemDetailsRepository;

    @InjectMocks
    private AdminInventoryServiceImpl inventoryService;

    private Item testItem;
    private ItemDetails testItemDetails;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .build();

        testItemDetails = ItemDetails.builder()
                .brand("TestBrand")
                .sku("SKU-001")
                .price(new BigDecimal("99.99"))
                .stockQuantity(50)
                .discountPercentage(BigDecimal.ZERO)
                .category(category)
                .build();

        testItem = Item.builder()
                .name("Test Product")
                .description("Test Description")
                .itemDetails(testItemDetails)
                .build();

        testItemDetails.setItem(testItem);
    }

    @Nested
    @DisplayName("getAllProductInventory()")
    class GetAllInventory {

        @Test
        @DisplayName("returns list of inventory items")
        void returnsListOfItems() {
            when(itemRepository.findAllWithItemDetails()).thenReturn(List.of(testItem));

            List<AdminProductInventoryResponse> result = inventoryService.getAllProductInventory();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).itemName()).isEqualTo("Test Product");
            assertThat(result.get(0).sku()).isEqualTo("SKU-001");
            assertThat(result.get(0).stockQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("returns empty list when no items")
        void returnsEmptyList() {
            when(itemRepository.findAllWithItemDetails()).thenReturn(List.of());

            List<AdminProductInventoryResponse> result = inventoryService.getAllProductInventory();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getLowStockProducts()")
    class GetLowStockProducts {

        @Test
        @DisplayName("returns low stock products")
        void returnsLowStockProducts() {
            testItemDetails.setStockQuantity(3);
            when(itemDetailsRepository.findLowStockProducts(5)).thenReturn(List.of(testItemDetails));

            List<AdminProductInventoryResponse> result = inventoryService.getLowStockProducts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).stockStatus()).isEqualTo(AdminProductInventoryResponse.StockStatus.LOW_STOCK);
        }
    }

    @Nested
    @DisplayName("getOutOfStockProducts()")
    class GetOutOfStockProducts {

        @Test
        @DisplayName("returns out of stock products")
        void returnsOutOfStockProducts() {
            testItemDetails.setStockQuantity(0);
            when(itemDetailsRepository.findOutOfStockProducts()).thenReturn(List.of(testItemDetails));

            List<AdminProductInventoryResponse> result = inventoryService.getOutOfStockProducts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).stockStatus()).isEqualTo(AdminProductInventoryResponse.StockStatus.OUT_OF_STOCK);
        }
    }

    @Nested
    @DisplayName("adjustStock()")
    class AdjustStock {

        @Test
        @DisplayName("increases stock successfully")
        void increasesStockSuccessfully() {
            StockAdjustmentRequest request = new StockAdjustmentRequest(10, "New shipment");
            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(itemDetailsRepository.save(any())).thenReturn(testItemDetails);

            AdminProductInventoryResponse result = inventoryService.adjustStock(1L, request);

            assertThat(result.stockQuantity()).isEqualTo(60);
            verify(itemDetailsRepository).save(testItemDetails);
        }

        @Test
        @DisplayName("decreases stock successfully")
        void decreasesStockSuccessfully() {
            StockAdjustmentRequest request = new StockAdjustmentRequest(-10, "Correction");
            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(itemDetailsRepository.save(any())).thenReturn(testItemDetails);

            AdminProductInventoryResponse result = inventoryService.adjustStock(1L, request);

            assertThat(result.stockQuantity()).isEqualTo(40);
        }

        @Test
        @DisplayName("throws exception when item not found")
        void throwsExceptionWhenItemNotFound() {
            StockAdjustmentRequest request = new StockAdjustmentRequest(10, "New shipment");
            when(itemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.adjustStock(999L, request))
                    .isInstanceOf(ItemNotFoundException.class);
        }

        @Test
        @DisplayName("throws exception when stock would go negative")
        void throwsExceptionWhenStockNegative() {
            StockAdjustmentRequest request = new StockAdjustmentRequest(-100, "Too much");
            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

            assertThatThrownBy(() -> inventoryService.adjustStock(1L, request))
                    .isInstanceOf(ItemStockNegativeException.class);
        }
    }

    @Nested
    @DisplayName("bulkUpdateStock()")
    class BulkUpdateStock {

        @Test
        @DisplayName("updates multiple items successfully")
        void updatesMultipleItems() {
            StockAdjustmentRequest req1 = new StockAdjustmentRequest(10, "Shipment 1");
            StockAdjustmentRequest req2 = new StockAdjustmentRequest(5, "Shipment 2");
            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(itemRepository.findById(2L)).thenReturn(Optional.of(testItem));
            when(itemDetailsRepository.save(any())).thenReturn(testItemDetails);

            java.util.Map<Long, StockAdjustmentRequest> adjustments = new java.util.HashMap<>();
            adjustments.put(1L, req1);
            adjustments.put(2L, req2);

            List<AdminProductInventoryResponse> result = inventoryService.bulkUpdateStock(adjustments);

            assertThat(result).hasSize(2);
            verify(itemDetailsRepository, times(2)).save(testItemDetails);
        }
    }

    @Nested
    @DisplayName("getInventoryDashboardSummary()")
    class GetInventoryDashboardSummary {

        @Test
        @DisplayName("returns summary with correct counts")
        void returnsSummaryWithCorrectCounts() {
            when(itemDetailsRepository.findAll()).thenReturn(List.of(testItemDetails));

            AdminInventoryService.InventoryDashboardSummary result = inventoryService.getInventoryDashboardSummary();

            assertThat(result.totalProducts()).isEqualTo(1);
            assertThat(result.totalStockUnits()).isEqualTo(50);
            assertThat(result.lowStockProducts()).isEqualTo(0);
            assertThat(result.outOfStockProducts()).isEqualTo(0);
        }
    }
}