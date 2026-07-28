package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.item.ItemRequest;
import com.pkmprojects.shoppiq.dto.item.ItemResponse;
import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.general.category.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.general.item.DuplicateItemException;
import com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerSuspendedException;
import com.pkmprojects.shoppiq.service.category.CategoryLookupService;
import com.pkmprojects.shoppiq.service.seller.SellerProductServiceImpl;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.item.ItemWriteService;
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
@DisplayName("SellerProductServiceImpl Tests")
class SellerProductServiceImplTest {

    @Mock
    private SellerLookupService sellerLookupService;
    @Mock
    private ItemLookupService itemLookupService;
    @Mock
    private ItemWriteService itemWriteService;
    @Mock
    private CategoryLookupService categoryLookupService;

    @InjectMocks
    private SellerProductServiceImpl sellerProductService;

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

    private ItemRequest sampleRequest() {
        return new ItemRequest(
                "Test Product", "Test Description", "TestBrand",
                "SKU-001", new BigDecimal("99.99"), 50,
                BigDecimal.ZERO, "http://img.test/product.jpg", testCategory.getId()
        );
    }

    @Nested
    @DisplayName("createProduct()")
    class CreateProduct {

        @Test
        @DisplayName("creates product successfully")
        void createsProductSuccessfully() {
            stubActiveSeller();
            when(itemLookupService.existsByItemDetailsSku("SKU-001")).thenReturn(false);
            when(itemLookupService.existsBySlug(anyString())).thenReturn(false);
            when(categoryLookupService.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
            when(itemWriteService.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

            ItemResponse result = sellerProductService.createProduct(sampleRequest(), testUser);

            assertThat(result.name()).isEqualTo("Test Product");
            assertThat(result.sku()).isEqualTo("SKU-001");
            verify(itemWriteService).save(any(Item.class));
        }

        @Test
        @DisplayName("throws DuplicateItemException when SKU exists")
        void throwsWhenSkuExists() {
            stubActiveSeller();
            when(itemLookupService.existsByItemDetailsSku("SKU-001")).thenReturn(true);

            assertThatThrownBy(() -> sellerProductService.createProduct(sampleRequest(), testUser))
                    .isInstanceOf(DuplicateItemException.class);
        }

        @Test
        @DisplayName("throws SellerNotFoundException when seller not found")
        void throwsWhenSellerNotFound() {
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerProductService.createProduct(sampleRequest(), testUser))
                    .isInstanceOf(SellerNotFoundException.class);
        }

        @Test
        @DisplayName("throws SellerSuspendedException when seller is suspended")
        void throwsWhenSellerSuspended() {
            testSeller.setSellerStatus(SellerStatus.SUSPENDED);
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.of(testSeller));

            assertThatThrownBy(() -> sellerProductService.createProduct(sampleRequest(), testUser))
                    .isInstanceOf(SellerSuspendedException.class);
        }

        @Test
        @DisplayName("throws SellerNotVerifiedException when seller not approved")
        void throwsWhenSellerNotApproved() {
            testSeller.setVerificationStatus(VerificationStatus.PENDING);
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.of(testSeller));

            assertThatThrownBy(() -> sellerProductService.createProduct(sampleRequest(), testUser))
                    .isInstanceOf(SellerNotVerifiedException.class);
        }

        @Test
        @DisplayName("throws CategoryNotFoundException when category not found")
        void throwsWhenCategoryNotFound() {
            stubActiveSeller();
            when(itemLookupService.existsByItemDetailsSku("SKU-001")).thenReturn(false);
            when(categoryLookupService.findById(testCategory.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerProductService.createProduct(sampleRequest(), testUser))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getMyProducts()")
    class GetMyProducts {

        @Test
        @DisplayName("returns paginated products")
        void returnsPaginatedProducts() {
            stubActiveSeller();
            Page<Item> page = new PageImpl<>(List.of(testItem), PageRequest.of(0, 10), 1);
            when(itemLookupService.findBySellerId(testSeller.getId(), 0, 10)).thenReturn(page);

            PageResponse<ItemResponse> result = sellerProductService.getMyProducts(testUser, 0, 10);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().getFirst().name()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("returns empty page when no products")
        void returnsEmptyPage() {
            stubActiveSeller();
            Page<Item> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(itemLookupService.findBySellerId(testSeller.getId(), 0, 10)).thenReturn(page);

            PageResponse<ItemResponse> result = sellerProductService.getMyProducts(testUser, 0, 10);

            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getMyProductById()")
    class GetMyProductById {

        @Test
        @DisplayName("returns product when found")
        void returnsProductWhenFound() {
            stubActiveSeller();
            when(itemLookupService.findByIdAndSellerId(testItem.getId(), testSeller.getId()))
                    .thenReturn(Optional.of(testItem));

            ItemResponse result = sellerProductService.getMyProductById(testItem.getId(), testUser);

            assertThat(result.name()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("throws ItemNotFoundException when product not found")
        void throwsWhenProductNotFound() {
            stubActiveSeller();
            when(itemLookupService.findByIdAndSellerId(999L, testSeller.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerProductService.getMyProductById(999L, testUser))
                    .isInstanceOf(ItemNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProduct {

        @Test
        @DisplayName("updates product successfully")
        void updatesProductSuccessfully() {
            stubActiveSeller();
            when(itemLookupService.findByIdAndSellerId(testItem.getId(), testSeller.getId()))
                    .thenReturn(Optional.of(testItem));
            when(itemLookupService.existsByItemDetailsSkuAndIdNot("SKU-001", testItem.getId()))
                    .thenReturn(false);
            when(itemLookupService.existsBySlug(anyString())).thenReturn(false);
            when(categoryLookupService.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
            when(itemWriteService.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

            ItemRequest request = new ItemRequest(
                    "Updated Product", "Updated Desc", "NewBrand",
                    "SKU-001", new BigDecimal("149.99"), 30,
                    new BigDecimal("10.00"), "http://img.test/new.jpg", testCategory.getId()
            );

            ItemResponse result = sellerProductService.updateProduct(testItem.getId(), request, testUser);

            assertThat(result.name()).isEqualTo("Updated Product");
            verify(itemWriteService).save(any(Item.class));
        }

        @Test
        @DisplayName("throws ItemNotFoundException when product not found")
        void throwsWhenProductNotFound() {
            stubActiveSeller();
            when(itemLookupService.findByIdAndSellerId(999L, testSeller.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerProductService.updateProduct(999L, sampleRequest(), testUser))
                    .isInstanceOf(ItemNotFoundException.class);
        }

        @Test
        @DisplayName("sets DRAFT status when updating a PUBLISHED product")
        void setsDraftWhenUpdatingPublished() {
            stubActiveSeller();
            testItem.setPublishingStatus(ProductPublishingStatus.PUBLISHED);
            when(itemLookupService.findByIdAndSellerId(testItem.getId(), testSeller.getId()))
                    .thenReturn(Optional.of(testItem));
            when(itemLookupService.existsByItemDetailsSkuAndIdNot("SKU-001", testItem.getId()))
                    .thenReturn(false);
            when(categoryLookupService.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
            when(itemWriteService.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

            sellerProductService.updateProduct(testItem.getId(), sampleRequest(), testUser);

            verify(itemWriteService).save(argThat(item ->
                    item.getPublishingStatus() == ProductPublishingStatus.DRAFT));
        }
    }

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("deletes product successfully")
        void deletesProductSuccessfully() {
            stubActiveSeller();
            when(itemLookupService.findByIdAndSellerId(testItem.getId(), testSeller.getId()))
                    .thenReturn(Optional.of(testItem));

            sellerProductService.deleteProduct(testItem.getId(), testUser);

            verify(itemWriteService).delete(testItem);
        }

        @Test
        @DisplayName("throws ItemNotFoundException when product not found")
        void throwsWhenProductNotFound() {
            stubActiveSeller();
            when(itemLookupService.findByIdAndSellerId(999L, testSeller.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerProductService.deleteProduct(999L, testUser))
                    .isInstanceOf(ItemNotFoundException.class);
        }
    }
}
