package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.seller.response.SellerDashboardResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.Seller;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.SellerSuspendedException;
import com.pkmprojects.shoppiq.repository.ItemDetailsRepository;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.repository.OrderItemRepository;
import com.pkmprojects.shoppiq.repository.OrderRepository;
import com.pkmprojects.shoppiq.repository.SellerRepository;
import com.pkmprojects.shoppiq.service.seller.SellerDashboardService;
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

    private final SellerRepository sellerRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemDetailsRepository itemDetailsRepository;

    public SellerDashboardServiceImpl(SellerRepository sellerRepository,
                                      ItemRepository itemRepository,
                                      OrderRepository orderRepository,
                                      OrderItemRepository orderItemRepository,
                                      ItemDetailsRepository itemDetailsRepository) {
        this.sellerRepository = sellerRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemDetailsRepository = itemDetailsRepository;
    }

    @Override
    public SellerDashboardResponse getDashboardSummary(User user) {
        Seller seller = findActiveSeller(user);
        Long sellerId = seller.getId();

        long totalProducts = itemRepository.countBySellerId(sellerId);
        long totalOrders = orderRepository.countDistinctBySellerId(sellerId);
        BigDecimal totalRevenue = orderItemRepository
                .sumRevenueBySellerIdAndPaymentStatus(sellerId, PaymentStatus.PAID);
        long lowStockProducts = itemDetailsRepository
                .findLowStockProductsBySellerId(LOW_STOCK_THRESHOLD, sellerId).size();
        long outOfStockProducts = itemDetailsRepository
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
        Seller seller = sellerRepository.findByUserId(user.getId())
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
