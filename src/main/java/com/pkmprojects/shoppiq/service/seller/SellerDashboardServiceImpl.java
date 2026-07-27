package com.pkmprojects.shoppiq.service.seller;

import com.pkmprojects.shoppiq.dto.seller.response.SellerDashboardResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.seller.Seller;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.general.seller.SellerSuspendedException;
import com.pkmprojects.shoppiq.repository.order.OrderItemRepository;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import com.pkmprojects.shoppiq.service.itemdetails.ItemDetailsLookupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Default implementation of {@link SellerDashboardService}.
 *
 * <p>
 * Computes seller dashboard metrics including product count, order count,
 * revenue, and stock alerts using dedicated repository queries.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class SellerDashboardServiceImpl implements SellerDashboardService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final SellerLookupService sellerLookupService;
    private final ItemLookupService itemLookupService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemDetailsLookupService itemDetailsLookupService;

    public SellerDashboardServiceImpl(SellerLookupService sellerLookupService,
                                      ItemLookupService itemLookupService,
                                      OrderRepository orderRepository,
                                      OrderItemRepository orderItemRepository,
                                      ItemDetailsLookupService itemDetailsLookupService) {
        this.sellerLookupService = sellerLookupService;
        this.itemLookupService = itemLookupService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemDetailsLookupService = itemDetailsLookupService;
    }

    @Override
    public SellerDashboardResponse getDashboardSummary(User user) {
        Seller seller = findActiveSeller(user);
        Long sellerId = seller.getId();

        long totalProducts = itemLookupService.countBySellerId(sellerId);
        long totalOrders = orderRepository.countDistinctBySellerId(sellerId);
        BigDecimal totalRevenue = orderItemRepository
                .sumRevenueBySellerIdAndPaymentStatus(sellerId, PaymentStatus.PAID);
        long lowStockProducts = itemDetailsLookupService
                .findLowStockProductsBySellerId(LOW_STOCK_THRESHOLD, sellerId).size();
        long outOfStockProducts = itemDetailsLookupService
                .findOutOfStockProductsBySellerId(sellerId).size();

        return SellerDashboardResponse.from(
                totalProducts, totalOrders,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                lowStockProducts, outOfStockProducts
        );
    }

    @Override
    public List<SellerOrderResponse> getRecentOrders(User user) {
        Seller seller = findActiveSeller(user);
        return orderRepository.findDistinctBySellerIdOrderByPlacedAtDesc(seller.getId())
                .stream()
                .limit(10)
                .map(order -> SellerOrderResponse.from(order, seller.getId()))
                .toList();
    }

    private Seller findActiveSeller(User user) {
        Seller seller = sellerLookupService.findByUserId(user.getId())
                .orElseThrow(() -> SellerNotFoundException.userId(user.getId()));

        if (seller.getSellerStatus() == SellerStatus.SUSPENDED) {
            throw SellerSuspendedException.forAction(seller.getId(), "view dashboard");
        }

        if (seller.getSellerStatus() != SellerStatus.ACTIVE
                || seller.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw SellerNotVerifiedException.forAction(seller.getId(), "view dashboard");
        }

        return seller;
    }
}
