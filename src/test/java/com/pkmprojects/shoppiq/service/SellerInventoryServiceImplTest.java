package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerInventoryResponse;
import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.general.inventory.ItemStockNegativeException;
import com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerSuspendedException;
import com.pkmprojects.shoppiq.service.seller.SellerInventoryServiceImpl;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsLookupService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsWriteService;
import com.pkmprojects.shoppiq.service.seller.SellerLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerInventoryServiceImpl Tests")
class SellerInventoryServiceImplTest {

    @Mock
    private SellerLookupService sellerLookupService;
    @Mock
    private ItemLookupService itemLookupService;
    @Mock
    private ItemDetailsLookupService itemDetailsLookupService;
    @Mock
    private ItemDetailsWriteService itemDetailsWriteService;

    @InjectMocks
    private SellerInventoryServiceImpl sellerInventoryService;

    private User testUser;
    private Seller testSeller;
    private Category testCategory;
    private ItemDetails testItemDetails;
    private Item testItem;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("seller@test.com")
                .build();

        testSeller = Seller.builder()
                .user(testUser)
                .sellerStatus(SellerStatus.ACTIVE)
                .verificationStatus(VerificationStatus.APPROVED)
                .build();

        testCategory = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .build();

        testItemDetails = ItemDetails.builder()
                .brand("TestBrand")
                .sku("SKU-001")
                .price(new BigDecimal("99.99"))
                .stockQuantity(50)
                .discountPercentage(BigDecimal.ZERO)
                .imageUrl("http://img.test/product.jpg")
                .category(testCategory)
                .build();

        testItem = Item.builder()
                .name("Test Product")
                .slug("test-product")
                .description("Test Description")
                .seller(testSeller)
                .publishingStatus(ProductPublishingStatus.DRAFT)
                .itemDetails(testItemDetails)
                .build();

        testItemDetails.setItem(testItem);
    }

    private void stubActiveSeller() {
        when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.of(testSeller));
    }

    @Nested
    @DisplayName("getInventory()")
    class GetInventory {

        @Test
        @DisplayName("returns paginated inventory")
        void returnsPaginatedInventory() {
            stubActiveSeller();
            Page<Item> page = new PageImpl<>(List.of(testItem), PageRequest.of(0, 10), 1);
            when(itemLookupService.findBySellerId(testSeller.getId(), 0, 10)).thenReturn(page);

            PageResponse<SellerInventoryResponse> result =
                    sellerInventoryService.getInventory(testUser, 0, 10);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).itemName()).isEqualTo("Test Product");
            assertThat(result.content().get(0).sku()).isEqualTo("SKU-001");
        }

        @Test
        @DisplayName("returns empty inventory")
        void returnsEmptyInventory() {
            stubActiveSeller();
            Page<Item> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(itemLookupService.findBySellerId(testSeller.getId(), 0, 10)).thenReturn(page);

            PageResponse<SellerInventoryResponse> result =
                    sellerInventoryService.getInventory(testUser, 0, 10);

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getLowStockProducts()")
    class GetLowStockProducts {

        @Test
        @DisplayName("returns low stock products")
        void returnsLowStockProducts() {
            stubActiveSeller();
            testItemDetails.setStockQuantity(3);
            when(itemDetailsLookupService.findLowStockProductsBySellerId(5, testSeller.getId()))
                    .thenReturn(List.of(testItemDetails));

            PageResponse<SellerInventoryResponse> result =
                    sellerInventoryService.getLowStockProducts(testUser, 0, 10);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).stockQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("returns empty list when no low stock products")
        void returnsEmptyWhenNoLowStock() {
            stubActiveSeller();
            when(itemDetailsLookupService.findLowStockProductsBySellerId(5, testSeller.getId()))
                    .thenReturn(List.of());

            PageResponse<SellerInventoryResponse> result =
                    sellerInventoryService.getLowStockProducts(testUser, 0, 10);

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getOutOfStockProducts()")
    class GetOutOfStockProducts {

        @Test
        @DisplayName("returns out of stock products")
        void returnsOutOfStockProducts() {
            stubActiveSeller();
            testItemDetails.setStockQuantity(0);
            when(itemDetailsLookupService.findOutOfStockProductsBySellerId(testSeller.getId()))
                    .thenReturn(List.of(testItemDetails));

            PageResponse<SellerInventoryResponse> result =
                    sellerInventoryService.getOutOfStockProducts(testUser, 0, 10);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).stockQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("returns empty list when all items in stock")
        void returnsEmptyWhenAllInStock() {
            stubActiveSeller();
            when(itemDetailsLookupService.findOutOfStockProductsBySellerId(testSeller.getId()))
                    .thenReturn(List.of());

            PageResponse<SellerInventoryResponse> result =
                    sellerInventoryService.getOutOfStockProducts(testUser, 0, 10);

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("adjustStock()")
    class AdjustStock {

        @Test
        @DisplayName("sets stock quantity successfully")
        void setsStockQuantitySuccessfully() {
            stubActiveSeller();
            when(itemLookupService.findByIdAndSellerId(testItem.getId(), testSeller.getId()))
                    .thenReturn(Optional.of(testItem));
            when(itemDetailsWriteService.save(any(ItemDetails.class))).thenAnswer(inv -> inv.getArgument(0));

            SellerInventoryResponse result =
                    sellerInventoryService.adjustStock(testItem.getId(), 10, "Restock", testUser);

            assertThat(result.stockQuantity()).isEqualTo(10);
            verify(itemDetailsWriteService).save(testItemDetails);
        }

        @Test
        @DisplayName("throws ItemNotFoundException when item not found")
        void throwsWhenItemNotFound() {
            stubActiveSeller();
            when(itemLookupService.findByIdAndSellerId(999L, testSeller.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    sellerInventoryService.adjustStock(999L, 10, "Restock", testUser))
                    .isInstanceOf(ItemNotFoundException.class);
        }

        @Test
        @DisplayName("throws ItemStockNegativeException for negative quantity")
        void throwsWhenNegativeQuantity() {
            stubActiveSeller();
            when(itemLookupService.findByIdAndSellerId(testItem.getId(), testSeller.getId()))
                    .thenReturn(Optional.of(testItem));

            assertThatThrownBy(() ->
                    sellerInventoryService.adjustStock(testItem.getId(), -10, "Remove", testUser))
                    .isInstanceOf(ItemStockNegativeException.class);
        }
    }

    @Nested
    @DisplayName("seller precondition checks")
    class SellerPreconditions {

        @Test
        @DisplayName("throws SellerNotFoundException when seller not found")
        void throwsWhenSellerNotFound() {
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    sellerInventoryService.getInventory(testUser, 0, 10))
                    .isInstanceOf(SellerNotFoundException.class);
        }

        @Test
        @DisplayName("throws SellerSuspendedException when seller is suspended")
        void throwsWhenSellerSuspended() {
            testSeller.setSellerStatus(SellerStatus.SUSPENDED);
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.of(testSeller));

            assertThatThrownBy(() ->
                    sellerInventoryService.getInventory(testUser, 0, 10))
                    .isInstanceOf(SellerSuspendedException.class);
        }

        @Test
        @DisplayName("throws SellerNotVerifiedException when seller not approved")
        void throwsWhenSellerNotApproved() {
            testSeller.setVerificationStatus(VerificationStatus.PENDING);
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.of(testSeller));

            assertThatThrownBy(() ->
                    sellerInventoryService.getInventory(testUser, 0, 10))
                    .isInstanceOf(SellerNotVerifiedException.class);
        }
    }
}
