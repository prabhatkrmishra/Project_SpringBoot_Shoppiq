package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.admin.report.*;
import com.pkmprojects.shoppiq.dto.admin.response.CommissionReportResponse;
import com.pkmprojects.shoppiq.entity.*;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.repository.*;
import com.pkmprojects.shoppiq.service.admin.AdminReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AdminReportService}.
 *
 * <p>
 * Provides report generation and export functionality for
 * sales, revenue, product, customer, and inventory reports.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Generate sales report with daily breakdown.</li>
 *     <li>Generate revenue report with payment method/status breakdown.</li>
 *     <li>Generate product performance report.</li>
 *     <li>Generate customer analytics report.</li>
 *     <li>Generate inventory health report.</li>
 *     <li>Export reports in PDF, Excel, CSV formats.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Uses constructor injection.</li>
 *     <li>All queries execute in read-only transactions.</li>
 *     <li>Export formats use Apache POI (Excel) and OpenPDF (PDF).</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class AdminReportServiceImpl implements AdminReportService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ItemRepository itemRepository;
    private final ItemDetailsRepository itemDetailsRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;

    public AdminReportServiceImpl(OrderRepository orderRepository,
                                  PaymentRepository paymentRepository,
                                  ItemRepository itemRepository,
                                  ItemDetailsRepository itemDetailsRepository,
                                  UserRepository userRepository,
                                  OrderItemRepository orderItemRepository,
                                  SellerRepository sellerRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.itemRepository = itemRepository;
        this.itemDetailsRepository = itemDetailsRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public SalesReport generateSalesReport(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Order> orders = orderRepository.findByPlacedAtBetween(startInstant, endInstant);

        long totalOrders;
        totalOrders = orders.size();
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Daily sales aggregation by date
        Map<LocalDate, SalesReport.DailySales> dailySales = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getPlacedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long count = list.size();
                                    BigDecimal revenue = list.stream()
                                            .map(Order::getGrandTotal)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return new SalesReport.DailySales(count, revenue);
                                }
                        )
                ));

        Map<OrderStatus, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        // Top products — aggregated from order items in Java
        List<SalesReport.TopProductSales> topProducts = orders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getItemDetails().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    long qty = items.stream().mapToInt(i -> i.getQuantity()).sum();
                                    BigDecimal revenue = items.stream()
                                            .map(i -> i.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(i.getQuantity())))
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return new SalesReport.TopProductSales(
                                            items.get(0).getItemDetails().getId(),
                                            items.get(0).getItemNameSnapshot(),
                                            items.get(0).getItemDetails().getSku(),
                                            qty, revenue
                                    );
                                }
                        )
                )).values().stream()
                .sorted(Comparator.comparing(SalesReport.TopProductSales::quantitySold).reversed())
                .limit(10)
                .toList();

        // Top categories — aggregated from order items in Java
        List<SalesReport.TopCategorySales> topCategories = orders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getItemDetails().getCategory().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    long qty = items.stream().mapToInt(i -> i.getQuantity()).sum();
                                    BigDecimal revenue = items.stream()
                                            .map(i -> i.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(i.getQuantity())))
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return new SalesReport.TopCategorySales(
                                            items.get(0).getItemDetails().getCategory().getId(),
                                            items.get(0).getItemDetails().getCategory().getName(),
                                            qty, revenue
                                    );
                                }
                        )
                )).values().stream()
                .sorted(Comparator.comparing(SalesReport.TopCategorySales::revenue).reversed())
                .limit(10)
                .toList();

        return new SalesReport(
                startDate, endDate, totalOrders, totalRevenue, dailySales, ordersByStatus,
                topProducts, topCategories
        );
    }

    @Override
    public RevenueReport generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Payment> payments = paymentRepository.findByCreatedAtBetweenAndPaymentStatusIn(
                startInstant, endInstant, List.of(PaymentStatus.PAID, PaymentStatus.REFUNDED));

        BigDecimal totalRevenue = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal refundedAmount = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.REFUNDED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossRevenue = totalRevenue.add(refundedAmount);

        // Daily revenue
        Map<LocalDate, BigDecimal> dailyRevenue = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .collect(Collectors.groupingBy(
                        p -> p.getPaidAt() != null
                                ? p.getPaidAt().atZone(ZoneId.systemDefault()).toLocalDate()
                                : p.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        Map<PaymentStatus, BigDecimal> revenueByPaymentStatus = payments.stream()
                .collect(Collectors.groupingBy(
                        Payment::getPaymentStatus,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        Map<String, BigDecimal> revenueByPaymentMethod = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .collect(Collectors.groupingBy(
                        p -> p.getPaymentMethod().name(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        // Estimate discounts, taxes, shipping from orders
        List<Order> orders = orderRepository.findByPlacedAtBetween(startInstant, endInstant);
        BigDecimal discounts = orders.stream().map(Order::getDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxes = orders.stream().map(Order::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shipping = orders.stream().map(Order::getShippingFee).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RevenueReport(
                startDate, endDate, totalRevenue, grossRevenue, discounts, taxes, shipping,
                dailyRevenue, revenueByPaymentStatus, revenueByPaymentMethod
        );
    }

    @Override
    public ProductReport generateProductReport(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Order> orders = orderRepository.findByPlacedAtBetween(startInstant, endInstant);

        // Product performance
        Map<Long, ProductPerformanceData> productMap = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getItemDetails().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    long qty = items.stream().mapToInt(i -> i.getQuantity()).sum();
                                    BigDecimal revenue = items.stream()
                                            .map(i -> i.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(i.getQuantity())))
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    BigDecimal avgPrice = qty > 0
                                            ? revenue.divide(BigDecimal.valueOf(qty), 2, java.math.RoundingMode.HALF_UP)
                                            : BigDecimal.ZERO;
                                    ItemDetails details = items.get(0).getItemDetails();
                                    return new ProductPerformanceData(
                                            details.getId(),
                                            details.getItem().getName(),
                                            details.getSku(),
                                            qty,
                                            revenue,
                                            avgPrice,
                                            details.getStockQuantity()
                                    );
                                }
                        )
                ));

        List<ProductReport.ProductPerformance> productPerformance = productMap.values().stream()
                .map(data -> new ProductReport.ProductPerformance(
                        data.itemId(), data.itemName(), data.sku(),
                        data.quantitySold(), data.revenue(), data.averagePrice(), data.currentStock()
                ))
                .sorted(Comparator.comparing(ProductReport.ProductPerformance::revenue).reversed())
                .toList();

        // Category performance
        Map<Long, CategoryPerformanceData> categoryMap = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getItemDetails().getCategory().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    long qty = items.stream().mapToInt(i -> i.getQuantity()).sum();
                                    BigDecimal revenue = items.stream()
                                            .map(i -> i.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(i.getQuantity())))
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    long uniqueProducts = items.stream()
                                            .map(i -> i.getItemDetails().getId())
                                            .distinct()
                                            .count();
                                    return new CategoryPerformanceData(
                                            items.get(0).getItemDetails().getCategory().getId(),
                                            items.get(0).getItemDetails().getCategory().getName(),
                                            qty, revenue, uniqueProducts
                                    );
                                }
                        )
                ));

        List<ProductReport.CategoryPerformance> categoryPerformance = categoryMap.values().stream()
                .map(data -> new ProductReport.CategoryPerformance(
                        data.categoryId(), data.categoryName(),
                        data.quantitySold(), data.revenue(), data.uniqueProductsSold()
                ))
                .sorted(Comparator.comparing(ProductReport.CategoryPerformance::revenue).reversed())
                .toList();

        return new ProductReport(
                startDate, endDate,
                productPerformance.stream().mapToLong(ProductReport.ProductPerformance::quantitySold).sum(),
                productPerformance.stream().map(ProductReport.ProductPerformance::revenue).reduce(BigDecimal.ZERO, BigDecimal::add),
                productPerformance, categoryPerformance
        );
    }

    @Override
    public CustomerReport generateCustomerReport(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<Order> orders = orderRepository.findByPlacedAtBetween(startInstant, endInstant);

        // Customer aggregation
        Map<Long, CustomerAggData> customerMap = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getUser().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                orderList -> {
                                    long count = orderList.size();
                                    BigDecimal spent = orderList.stream()
                                            .map(Order::getGrandTotal)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    BigDecimal avgOrder = count > 0
                                            ? spent.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP)
                                            : BigDecimal.ZERO;
                                    LocalDate firstOrder = orderList.stream()
                                            .map(o -> o.getPlacedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                                            .min(LocalDate::compareTo)
                                            .orElse(LocalDate.now());
                                    LocalDate lastOrder = orderList.stream()
                                            .map(o -> o.getPlacedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                                            .max(LocalDate::compareTo)
                                            .orElse(LocalDate.now());
                                    return new CustomerAggData(
                                            orderList.get(0).getUser().getId(),
                                            orderList.get(0).getUser().getUsername(),
                                            orderList.get(0).getUser().getEmail(),
                                            count, spent, avgOrder, firstOrder, lastOrder
                                    );
                                }
                        )
                ));

        List<CustomerReport.TopCustomer> topCustomers = customerMap.values().stream()
                .map(data -> new CustomerReport.TopCustomer(
                        data.userId(), data.username(), data.email(),
                        data.orderCount(), data.totalSpent(), data.firstOrder(), data.lastOrder()
                ))
                .sorted(Comparator.comparing(CustomerReport.TopCustomer::totalSpent).reversed())
                .limit(20)
                .toList();

        // Segments
        List<CustomerReport.CustomerSegment> segments = List.of(
                new CustomerReport.CustomerSegment("VIP", customerMap.values().stream().filter(d -> d.totalSpent().compareTo(BigDecimal.valueOf(10000)) > 0).count(),
                        customerMap.values().stream().filter(d -> d.totalSpent().compareTo(BigDecimal.valueOf(10000)) > 0)
                                .map(CustomerAggData::totalSpent).reduce(BigDecimal.ZERO, BigDecimal::add)),
                new CustomerReport.CustomerSegment("Regular", customerMap.values().stream().filter(d -> d.totalSpent().compareTo(BigDecimal.valueOf(1000)) > 0
                        && d.totalSpent().compareTo(BigDecimal.valueOf(10000)) <= 0).count(),
                        customerMap.values().stream().filter(d -> d.totalSpent().compareTo(BigDecimal.valueOf(1000)) > 0
                                        && d.totalSpent().compareTo(BigDecimal.valueOf(10000)) <= 0)
                                .map(CustomerAggData::totalSpent).reduce(BigDecimal.ZERO, BigDecimal::add)),
                new CustomerReport.CustomerSegment("New", customerMap.values().stream().filter(d -> d.totalSpent().compareTo(BigDecimal.valueOf(1000)) <= 0).count(),
                        customerMap.values().stream().filter(d -> d.totalSpent().compareTo(BigDecimal.valueOf(1000)) <= 0)
                                .map(CustomerAggData::totalSpent).reduce(BigDecimal.ZERO, BigDecimal::add))
        );

        long totalCustomers = userRepository.count();
        long newCustomers = userRepository.countByCreatedAtAfter(startInstant);
        long activeCustomers = customerMap.size();
        long returningCustomers = activeCustomers - newCustomers;
        BigDecimal totalRevenue = topCustomers.stream().map(CustomerReport.TopCustomer::totalSpent).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgOrderValue = activeCustomers > 0
                ? totalRevenue.divide(BigDecimal.valueOf(activeCustomers), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new CustomerReport(
                startDate, endDate, totalCustomers, newCustomers, returningCustomers,
                totalRevenue, avgOrderValue, segments, topCustomers
        );
    }

    @Override
    public InventoryReport generateInventoryReport() {
        List<ItemDetails> allDetails = itemDetailsRepository.findAll();

        int totalProducts = allDetails.size();
        int totalStockUnits = allDetails.stream().mapToInt(ItemDetails::getStockQuantity).sum();
        BigDecimal totalInventoryValue = allDetails.stream()
                .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long outOfStockCount = allDetails.stream().filter(d -> d.getStockQuantity() == 0).count();
        long lowStockCount = allDetails.stream().filter(d -> d.getStockQuantity() > 0 && d.getStockQuantity() <= 5).count();

        List<InventoryReport.ProductInventoryStatus> productStatuses = allDetails.stream()
                .map(d -> {
                    String status = d.getStockQuantity() == 0 ? "OUT_OF_STOCK"
                            : d.getStockQuantity() <= 5 ? "LOW_STOCK" : "IN_STOCK";
                    return new InventoryReport.ProductInventoryStatus(
                            d.getId(), d.getItem().getName(), d.getSku(),
                            d.getCategory().getName(), d.getStockQuantity(),
                            d.getPrice(), d.getPrice().multiply(BigDecimal.valueOf(d.getStockQuantity())),
                            status
                    );
                })
                .sorted(Comparator.comparing(InventoryReport.ProductInventoryStatus::stockStatus)
                        .thenComparing(InventoryReport.ProductInventoryStatus::stockQuantity))
                .toList();

        return new InventoryReport(
                LocalDate.now(), totalProducts, totalStockUnits, totalInventoryValue,
                lowStockCount, outOfStockCount, productStatuses
        );
    }

    @Override
    public List<CommissionReportResponse> generateCommissionReport() {
        List<Object[]> results = orderItemRepository.aggregateRevenueBySeller(PaymentStatus.PAID);
        List<CommissionReportResponse> reports = new ArrayList<>();

        for (Object[] row : results) {
            Long sellerId = (Long) row[0];
            String businessName = (String) row[1];
            long totalOrders = ((Number) row[2]).longValue();
            BigDecimal totalRevenue = (BigDecimal) row[3];

            var seller = sellerRepository.findById(sellerId)
                    .orElseThrow(() -> new RuntimeException("Seller not found: " + sellerId));
            BigDecimal commissionRate = seller.getCommissionRate() != null
                    ? seller.getCommissionRate()
                    : BigDecimal.ZERO;

            reports.add(CommissionReportResponse.from(
                    sellerId, businessName, totalOrders, totalRevenue, commissionRate
            ));
        }

        return reports;
    }

    @Override
    public byte[] exportReport(ReportType reportType, ExportFormat format, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement actual export using Apache POI (Excel), OpenPDF (PDF), OpenCSV (CSV)
        // For now, return placeholder
        return "Report export not yet implemented".getBytes();
    }

}