package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.seller.response.SellerDashboardResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerSuspendedException;
import com.pkmprojects.shoppiq.repository.order.OrderItemRepository;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.service.seller.SellerDashboardServiceImpl;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsLookupService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerDashboardServiceImpl Tests")
class SellerDashboardServiceImplTest {

    @Mock
    private SellerLookupService sellerLookupService;
    @Mock
    private ItemLookupService itemLookupService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ItemDetailsLookupService itemDetailsLookupService;

    @InjectMocks
    private SellerDashboardServiceImpl sellerDashboardService;

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
    @DisplayName("getDashboardSummary()")
    class GetDashboardSummary {

        @Test
        @DisplayName("returns dashboard summary with all metrics")
        void returnsDashboardSummary() {
            stubActiveSeller();
            when(itemLookupService.countBySellerId(testSeller.getId())).thenReturn(5L);
            when(orderRepository.countDistinctBySellerId(testSeller.getId())).thenReturn(3L);
            when(orderItemRepository.sumRevenueBySellerIdAndPaymentStatus(testSeller.getId(), PaymentStatus.PAID))
                    .thenReturn(new BigDecimal("500.00"));
            when(itemDetailsLookupService.countLowStockProductsBySellerId(5, testSeller.getId()))
                    .thenReturn(1L);
            when(itemDetailsLookupService.countOutOfStockProductsBySellerId(testSeller.getId()))
                    .thenReturn(0L);

            SellerDashboardResponse result = sellerDashboardService.getDashboardSummary(testUser);

            assertThat(result.totalProducts()).isEqualTo(5);
            assertThat(result.totalOrders()).isEqualTo(3);
            assertThat(result.totalRevenue()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(result.lowStockProducts()).isEqualTo(1);
            assertThat(result.outOfStockProducts()).isEqualTo(0);
        }

        @Test
        @DisplayName("returns zero revenue when no paid orders")
        void returnsZeroRevenueWhenNoPaidOrders() {
            stubActiveSeller();
            when(itemLookupService.countBySellerId(testSeller.getId())).thenReturn(0L);
            when(orderRepository.countDistinctBySellerId(testSeller.getId())).thenReturn(0L);
            when(orderItemRepository.sumRevenueBySellerIdAndPaymentStatus(testSeller.getId(), PaymentStatus.PAID))
                    .thenReturn(null);
            when(itemDetailsLookupService.countLowStockProductsBySellerId(5, testSeller.getId()))
                    .thenReturn(0L);
            when(itemDetailsLookupService.countOutOfStockProductsBySellerId(testSeller.getId()))
                    .thenReturn(0L);

            SellerDashboardResponse result = sellerDashboardService.getDashboardSummary(testUser);

            assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getRecentOrders()")
    class GetRecentOrders {

        @Test
        @DisplayName("returns recent orders")
        void returnsRecentOrders() {
            stubActiveSeller();
            Order order = mock(Order.class);
            when(order.getOrderItems()).thenReturn(List.of());
            when(orderRepository.findDistinctBySellerId(eq(testSeller.getId()), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of(order)));

            List<SellerOrderResponse> result = sellerDashboardService.getRecentOrders(testUser);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("returns empty list when no orders")
        void returnsEmptyListWhenNoOrders() {
            stubActiveSeller();
            when(orderRepository.findDistinctBySellerId(eq(testSeller.getId()), any(PageRequest.class)))
                    .thenReturn(Page.empty());

            List<SellerOrderResponse> result = sellerDashboardService.getRecentOrders(testUser);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("seller precondition checks")
    class SellerPreconditions {

        @Test
        @DisplayName("throws SellerNotFoundException when seller not found")
        void throwsWhenSellerNotFound() {
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerDashboardService.getDashboardSummary(testUser))
                    .isInstanceOf(SellerNotFoundException.class);
        }

        @Test
        @DisplayName("throws SellerSuspendedException when seller is suspended")
        void throwsWhenSellerSuspended() {
            testSeller.setSellerStatus(SellerStatus.SUSPENDED);
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.of(testSeller));

            assertThatThrownBy(() -> sellerDashboardService.getDashboardSummary(testUser))
                    .isInstanceOf(SellerSuspendedException.class);
        }

        @Test
        @DisplayName("throws SellerNotVerifiedException when seller not approved")
        void throwsWhenSellerNotApproved() {
            testSeller.setVerificationStatus(VerificationStatus.PENDING);
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.of(testSeller));

            assertThatThrownBy(() -> sellerDashboardService.getDashboardSummary(testUser))
                    .isInstanceOf(SellerNotVerifiedException.class);
        }

        @Test
        @DisplayName("getRecentOrders throws when seller not found")
        void getRecentOrdersThrowsWhenSellerNotFound() {
            when(sellerLookupService.findByUserId(testUser.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sellerDashboardService.getRecentOrders(testUser))
                    .isInstanceOf(SellerNotFoundException.class);
        }
    }
}
