package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.admin.analytics.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.admin.AdminDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AdminDashboardService}.
 *
 * <p>
 * Provides aggregated dashboard statistics, sales analytics, and
 * recent activity feeds by querying multiple repositories.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Calculate dashboard summary metrics.</li>
 *     <li>Generate time-series sales analytics.</li>
 *     <li>Compile recent activity from multiple sources.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>All queries execute in read-only transactions.</li>
 *     <li>Aggregates data from User, Order, Payment, Item, ItemReview repositories.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ItemRepository itemRepository;
    private final ItemDetailsRepository itemDetailsRepository;
    private final ItemReviewRepository itemReviewRepository;

    public AdminDashboardServiceImpl(UserRepository userRepository,
                                     OrderRepository orderRepository,
                                     PaymentRepository paymentRepository,
                                     ItemRepository itemRepository,
                                     ItemDetailsRepository itemDetailsRepository,
                                     ItemReviewRepository itemReviewRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.itemRepository = itemRepository;
        this.itemDetailsRepository = itemDetailsRepository;
        this.itemReviewRepository = itemReviewRepository;
    }

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        long totalUsers = userRepository.count();
        long totalProducts = itemRepository.count();
        long totalOrders = orderRepository.count();

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        long todaysOrders = orderRepository.countByPlacedAtBetween(startOfDay, endOfDay);

        BigDecimal todaysRevenue = paymentRepository.sumAmountByStatusAndDateRange(
                PaymentStatus.PAID, startOfDay, endOfDay);

        long pendingOrders = orderRepository.countByStatus(OrderStatus.PLACED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        List<ItemDetails> allItemDetails = itemDetailsRepository.findAll();
        long outOfStockProducts = allItemDetails.stream()
                .filter(d -> d.getStockQuantity() == 0)
                .count();
        long lowStockProducts = allItemDetails.stream()
                .filter(d -> d.getStockQuantity() > 0 && d.getStockQuantity() <= LOW_STOCK_THRESHOLD)
                .count();

        return DashboardSummaryResponse.from(
                totalUsers,
                totalProducts,
                totalOrders,
                todaysOrders,
                todaysRevenue != null ? todaysRevenue : BigDecimal.ZERO,
                pendingOrders,
                cancelledOrders,
                outOfStockProducts,
                lowStockProducts
        );
    }

    @Override
    public SalesAnalyticsResponse getSalesAnalytics() {
        LocalDate endDate = LocalDate.now(ZoneId.systemDefault());
        LocalDate startDate = endDate.minusDays(30);
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Order> orders = orderRepository.findByPlacedAtBetweenOrderByPlacedAtAsc(startInstant, endInstant);

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
                    OrderItem first = e.getValue().get(0);
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
                            e.getKey(), e.getValue().get(0).getItemDetails().getCategory().getName(),
                            qty, revenue);
                })
                .sorted(Comparator.comparing(TopCategoryData::totalRevenue).reversed())
                .limit(10)
                .toList();

        List<Payment> payments = paymentRepository.findByPaymentStatusAndPaidAtBetweenOrderByPaidAtAsc(
                PaymentStatus.PAID, startInstant, endInstant);

        Map<LocalDate, BigDecimal> revenueTrends = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaidAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        return new SalesAnalyticsResponse(
                dailySales, weeklySales, monthlySales,
                topSellingProducts, topCategories, revenueTrends
        );
    }

    @Override
    public RecentActivityResponse getRecentActivity() {
        // Recent orders (last 10)
        List<Order> recentOrders = orderRepository.findTop10ByOrderByPlacedAtDesc();
        List<RecentActivityResponse.RecentOrderData> recentOrderData = recentOrders.stream()
                .map(order -> new RecentActivityResponse.RecentOrderData(
                        order.getId(),
                        order.getUser().getUsername(),
                        order.getStatus().name(),
                        order.getGrandTotal(),
                        order.getPlacedAt()
                ))
                .toList();

        // Recent payments (last 10)
        List<Payment> recentPayments = paymentRepository.findTop10ByOrderByCreatedAtDesc();
        List<RecentActivityResponse.RecentPaymentData> recentPaymentData = recentPayments.stream()
                .map(payment -> new RecentActivityResponse.RecentPaymentData(
                        payment.getId(),
                        payment.getPaymentReference(),
                        payment.getOrder().getUser().getUsername(),
                        payment.getPaymentStatus().name(),
                        payment.getAmount(),
                        payment.getCreatedAt()
                ))
                .toList();

        // Recent reviews (last 10)
        List<ItemReview> recentReviews = itemReviewRepository.findTop10ByOrderByCreatedAtDesc();
        List<RecentActivityResponse.RecentReviewData> recentReviewData = recentReviews.stream()
                .map(review -> new RecentActivityResponse.RecentReviewData(
                        review.getId(),
                        review.getItem().getName(),
                        review.getUser().getUsername(),
                        review.getRating(),
                        review.getCreatedAt()
                ))
                .toList();

        // Recent users (last 10)
        List<User> recentUsers = userRepository.findTop10ByOrderByCreatedAtDesc();
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