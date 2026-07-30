package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.config.InventoryConstants;
import com.pkmprojects.shoppiq.dto.admin.analytics.*;
import com.pkmprojects.shoppiq.dto.admin.response.DashboardSummaryResponse;
import com.pkmprojects.shoppiq.dto.admin.response.RecentActivityResponse;
import com.pkmprojects.shoppiq.dto.admin.response.SalesAnalyticsResponse;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.order.OrderItem;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.service.admin.readmodel.AdminOrderReadModel;
import com.pkmprojects.shoppiq.service.admin.readmodel.AdminPaymentReadModel;
import com.pkmprojects.shoppiq.service.admin.readmodel.AdminProductReadModel;
import com.pkmprojects.shoppiq.service.admin.readmodel.AdminUserReadModel;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link AdminDashboardService} implementation that computes dashboard summary
 * statistics, time-series sales analytics, and recent activity feeds.
 *
 * <p>Aggregates data through ReadModel facades for the admin dashboard.</p>
 *
 * @author prabhatkrmishra
 * @see AdminDashboardService
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminUserReadModel userReadModel;
    private final AdminOrderReadModel orderReadModel;
    private final AdminPaymentReadModel paymentReadModel;
    private final AdminProductReadModel productReadModel;
    private final Clock clock;

    public AdminDashboardServiceImpl(AdminUserReadModel userReadModel,
                                     AdminOrderReadModel orderReadModel,
                                     AdminPaymentReadModel paymentReadModel,
                                     AdminProductReadModel productReadModel,
                                     Clock clock) {
        this.userReadModel = userReadModel;
        this.orderReadModel = orderReadModel;
        this.paymentReadModel = paymentReadModel;
        this.productReadModel = productReadModel;
        this.clock = clock;
    }

    /**
     * Computes the admin dashboard summary with user, product, order, revenue, and stock counts.
     *
     * @return dashboard summary response
     */
    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        long totalUsers = userReadModel.countAll();
        long totalProducts = productReadModel.countItems();
        long totalOrders = orderReadModel.countAll();

        LocalDate today = LocalDate.now(clock);
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        long todaysOrders = orderReadModel.countPlacedBetween(startOfDay, endOfDay);

        BigDecimal todaysRevenue = paymentReadModel.sumAmountByStatusAndDateRange(
                PaymentStatus.PAID, startOfDay, endOfDay);

        BigDecimal totalRevenue = paymentReadModel.sumAmountByStatus(PaymentStatus.PAID);

        long pendingOrders = orderReadModel.countByStatus(OrderStatus.PLACED);
        long cancelledOrders = orderReadModel.countByStatus(OrderStatus.CANCELLED);

        long outOfStockProducts = productReadModel.countOutOfStock();
        long lowStockProducts = productReadModel.countLowStock(InventoryConstants.LOW_STOCK_THRESHOLD);

        return DashboardSummaryResponse.from(
                totalUsers,
                totalProducts,
                totalOrders,
                todaysOrders,
                todaysRevenue,
                totalRevenue,
                pendingOrders,
                cancelledOrders,
                outOfStockProducts,
                lowStockProducts
        );
    }

    /**
     * Generates time-series sales analytics (daily/weekly/monthly), top-selling products,
     * top categories, and revenue trends for the last 30 days.
     *
     * @return sales analytics response
     */
    @Override
    public SalesAnalyticsResponse getSalesAnalytics() {
        LocalDate today = LocalDate.now(clock);
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        LocalDate startDate = today.minusDays(30);
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Order> orders = orderReadModel.findPlacedBetweenAsc(startInstant, endInstant, OrderStatus.DELIVERED, Pageable.unpaged());

        Map<LocalDate, List<Order>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getPlacedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<DailySalesData> dailySales = ordersByDate.entrySet().stream()
                .map(e -> new DailySalesData(
                        e.getKey(),
                        (long) e.getValue().size(),
                        e.getValue().stream().map(Order::getGrandTotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .toList();

        List<WeeklySalesData> weeklySales = ordersByDate.entrySet().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getKey().with(java.time.DayOfWeek.MONDAY),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(e -> new WeeklySalesData(
                        e.getKey().getYear(),
                        e.getKey().get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                        e.getValue().stream().mapToLong(entry -> entry.getValue().size()).sum(),
                        e.getValue().stream().flatMap(entry -> entry.getValue().stream())
                                .map(Order::getGrandTotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .sorted(Comparator.comparing(WeeklySalesData::year).thenComparing(WeeklySalesData::week))
                .toList();

        List<MonthlySalesData> monthlySales = ordersByDate.entrySet().stream()
                .collect(Collectors.groupingBy(
                        e -> java.time.YearMonth.from(e.getKey()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(e -> new MonthlySalesData(
                        e.getKey().getYear(),
                        e.getKey().getMonthValue(),
                        e.getValue().stream().mapToLong(entry -> entry.getValue().size()).sum(),
                        e.getValue().stream().flatMap(entry -> entry.getValue().stream())
                                .map(Order::getGrandTotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .sorted(Comparator.comparing(MonthlySalesData::year).thenComparing(MonthlySalesData::month))
                .toList();

        Map<Long, List<OrderItem>> itemsByProduct = orders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(oi -> oi.getItemDetails().getId()));

        List<TopSellingProductData> topSellingProducts = itemsByProduct.entrySet().stream()
                .map(e -> {
                    long qty = e.getValue().stream().mapToInt(OrderItem::getQuantity).sum();
                    BigDecimal revenue = e.getValue().stream()
                            .map(oi -> oi.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(oi.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    OrderItem first = e.getValue().getFirst();
                    return new TopSellingProductData(
                            e.getKey(), first.getItemNameSnapshot(),
                            first.getItemDetails().getSku(), qty, revenue);
                })
                .sorted(Comparator.comparing(TopSellingProductData::totalQuantitySold).reversed())
                .limit(10)
                .toList();

        Map<Long, List<OrderItem>> itemsByCategory = orders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(oi -> oi.getItemDetails().getCategory().getId()));

        List<TopCategoryData> topCategories = itemsByCategory.entrySet().stream()
                .map(e -> {
                    long qty = e.getValue().stream().mapToInt(OrderItem::getQuantity).sum();
                    BigDecimal revenue = e.getValue().stream()
                            .map(oi -> oi.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(oi.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new TopCategoryData(
                            e.getKey(), e.getValue().getFirst().getItemDetails().getCategory().getName(),
                            qty, revenue);
                })
                .sorted(Comparator.comparing(TopCategoryData::totalRevenue).reversed())
                .limit(10)
                .toList();

        List<Payment> payments = paymentReadModel.findPaidBetweenAsc(startInstant, endInstant);

        Map<LocalDate, BigDecimal> revenueTrends = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaidAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        Instant weekStart = today.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant monthStart = today.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        BigDecimal todayRevenue = paymentReadModel.sumAmountByStatusAndDateRange(
                PaymentStatus.PAID, startOfDay, endOfDay);
        BigDecimal weekRevenue = paymentReadModel.sumAmountByStatusAndDateRange(
                PaymentStatus.PAID, weekStart, endOfDay);
        BigDecimal monthRevenue = paymentReadModel.sumAmountByStatusAndDateRange(
                PaymentStatus.PAID, monthStart, endOfDay);

        long todayOrders = orderReadModel.countPlacedBetween(startOfDay, endOfDay);
        long weekOrders = orderReadModel.countPlacedBetween(weekStart, endOfDay, OrderStatus.DELIVERED);
        long monthOrders = orderReadModel.countPlacedBetween(monthStart, endOfDay, OrderStatus.DELIVERED);

        return new SalesAnalyticsResponse(
                dailySales, weeklySales, monthlySales,
                topSellingProducts, topCategories, revenueTrends,
                todayRevenue, weekRevenue, monthRevenue,
                todayOrders, weekOrders, monthOrders
        );
    }

    /**
     * Collects the 10 most recent orders, payments, reviews, and user registrations.
     *
     * @return recent activity response
     */
    @Override
    public RecentActivityResponse getRecentActivity() {
        List<Order> recentOrders = orderReadModel.findRecentTop10();
        List<RecentActivityResponse.RecentOrderData> recentOrderData = recentOrders.stream()
                .map(order -> new RecentActivityResponse.RecentOrderData(
                        order.getId(),
                        order.getUser().getUsername(),
                        order.getStatus().name(),
                        order.getGrandTotal(),
                        order.getPlacedAt()
                ))
                .toList();

        List<Payment> recentPayments = paymentReadModel.findRecentTop10();
        List<RecentActivityResponse.RecentPaymentData> recentPaymentData = recentPayments.stream()
                .map(payment -> new RecentActivityResponse.RecentPaymentData(
                        payment.getId(),
                        payment.getPaymentReference(),
                        payment.getOrder() != null && payment.getOrder().getUser() != null
                                ? payment.getOrder().getUser().getUsername() : "Unknown",
                        payment.getPaymentStatus().name(),
                        payment.getAmount(),
                        payment.getCreatedAt()
                ))
                .toList();

        List<ItemReview> recentReviews = productReadModel.findRecentReviewsTop10();
        List<RecentActivityResponse.RecentReviewData> recentReviewData = recentReviews.stream()
                .map(review -> new RecentActivityResponse.RecentReviewData(
                        review.getId(),
                        review.getItem() != null ? review.getItem().getName() : "Unknown",
                        review.getUser().getUsername(),
                        review.getRating(),
                        review.getCreatedAt()
                ))
                .toList();

        List<User> recentUsers = userReadModel.findRecentTop10();
        List<RecentActivityResponse.RecentUserData> recentUserData = recentUsers.stream()
                .map(user -> new RecentActivityResponse.RecentUserData(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getCreatedAt()
                ))
                .toList();

        return new RecentActivityResponse(
                recentOrderData,
                recentPaymentData,
                recentReviewData,
                recentUserData
        );
    }
}
