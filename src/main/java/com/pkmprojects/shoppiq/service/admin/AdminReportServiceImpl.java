package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.dto.admin.report.*;
import com.pkmprojects.shoppiq.dto.admin.response.CommissionReportResponse;
import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.config.InventoryConstants;
import com.pkmprojects.shoppiq.exception.general.seller.SellerNotFoundException;
import com.pkmprojects.shoppiq.repository.order.projection.*;
import com.pkmprojects.shoppiq.service.admin.readmodel.AdminOrderReadModel;
import com.pkmprojects.shoppiq.service.admin.readmodel.AdminPaymentReadModel;
import com.pkmprojects.shoppiq.service.admin.readmodel.AdminProductReadModel;
import com.pkmprojects.shoppiq.service.admin.readmodel.AdminUserReadModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link AdminReportService}
 * containing business logic for admin report generation.
 *
 * <p>Generates sales, revenue, product performance, customer, inventory, and
 * commission reports by aggregating data through ReadModel facades. Used by
 * {@code AdminReportController}.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional(readOnly = true)</strong> — All report methods are read-only, optimized for complex aggregation queries.</li>
 *   <li><strong>Constructor injection</strong> — final fields for immutability and testability.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see AdminReportService
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class AdminReportServiceImpl implements AdminReportService {

    private final AdminOrderReadModel orderReadModel;
    private final AdminPaymentReadModel paymentReadModel;
    private final AdminProductReadModel productReadModel;
    private final AdminUserReadModel userReadModel;
    private final Clock clock;

    public AdminReportServiceImpl(AdminOrderReadModel orderReadModel,
                                  AdminPaymentReadModel paymentReadModel,
                                  AdminProductReadModel productReadModel,
                                  AdminUserReadModel userReadModel,
                                  Clock clock) {
        this.orderReadModel = orderReadModel;
        this.paymentReadModel = paymentReadModel;
        this.productReadModel = productReadModel;
        this.userReadModel = userReadModel;
        this.clock = clock;
    }

    @Override
    public SalesReport generateSalesReport(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Lightweight query — returns [placedAt, grandTotal, status] tuples
        // instead of loading the full Order entity graph (orderItems, itemDetails,
        // category, item, user).
        List<Object[]> orderData = orderReadModel.findOrderValuesAndStatusBetween(startInstant, endInstant);

        long totalOrders = orderData.size();
        BigDecimal totalRevenue = orderData.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Daily sales — group lightweight tuples by date
        Map<LocalDate, SalesReport.DailySales> dailySales = orderData.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Instant) row[0]).atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long count = list.size();
                                    BigDecimal revenue = list.stream()
                                            .map(r -> (BigDecimal) r[1])
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return new SalesReport.DailySales(count, revenue);
                                }
                        )
                ));

        // Status distribution — group lightweight tuples by status
        Map<OrderStatus, Long> ordersByStatus = orderData.stream()
                .collect(Collectors.groupingBy(
                        row -> (OrderStatus) row[2],
                        Collectors.counting()
                ));

        // Top products — DB-aggregated via GROUP BY
        List<ProductSalesAggregate> productAggs = orderReadModel.aggregateProductSalesByDateRange(startInstant, endInstant);
        List<SalesReport.TopProductSales> topProducts = productAggs.stream()
                .limit(10)
                .map(a -> new SalesReport.TopProductSales(
                        a.getItemId(), a.getItemName(), a.getSku(),
                        a.getQuantitySold(), a.getRevenue()))
                .toList();

        // Top categories — DB-aggregated via GROUP BY
        List<CategorySalesAggregate> categoryAggs = orderReadModel.aggregateCategorySalesByDateRange(startInstant, endInstant);
        List<SalesReport.TopCategorySales> topCategories = categoryAggs.stream()
                .limit(10)
                .map(a -> new SalesReport.TopCategorySales(
                        a.getCategoryId(), a.getCategoryName(),
                        a.getQuantitySold(), a.getRevenue()))
                .toList();

        // averageOrderValue is computed but not used in this report variant;
        // kept for future enhancement or downstream callers
        return new SalesReport(
                startDate, endDate, totalOrders, totalRevenue, dailySales, ordersByStatus,
                topProducts, topCategories
        );
    }

    @Override
    public RevenueReport generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Aggregate payment amounts by status — single scalar queries instead of loading full entities
        BigDecimal totalRevenue = paymentReadModel.sumAmountByStatusAndDateRange(
                PaymentStatus.PAID, startInstant, endInstant);
        BigDecimal refundedAmount = paymentReadModel.sumAmountByStatusAndDateRange(
                PaymentStatus.REFUNDED, startInstant, endInstant);
        BigDecimal grossRevenue = totalRevenue.add(refundedAmount);

        // Daily revenue — DB-aggregated [paidAt, amount] tuples, grouped by date in Java
        List<Object[]> dailyRevData = paymentReadModel.aggregateDailyRevenueBetween(startInstant, endInstant);
        Map<LocalDate, BigDecimal> dailyRevenue = dailyRevData.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Instant) row[0]).atZone(ZoneId.systemDefault()).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, r -> (BigDecimal) r[1], BigDecimal::add)
                ));

        // Revenue by payment status — two known statuses in this context
        Map<PaymentStatus, BigDecimal> revenueByPaymentStatus = new EnumMap<>(PaymentStatus.class);
        revenueByPaymentStatus.put(PaymentStatus.PAID, totalRevenue);
        revenueByPaymentStatus.put(PaymentStatus.REFUNDED, refundedAmount);

        // Revenue by payment method — DB-aggregated [paymentMethod, amount] tuples
        List<Object[]> methodData = paymentReadModel.aggregateRevenueByPaymentMethodBetween(startInstant, endInstant);
        Map<String, BigDecimal> revenueByPaymentMethod = new LinkedHashMap<>();
        for (Object[] row : methodData) {
            revenueByPaymentMethod.put(((PaymentMethod) row[0]).name(), (BigDecimal) row[1]);
        }

        // Order-level aggregates (discounts, taxes, shipping) — single lightweight query
        List<Object[]> charges = orderReadModel.aggregateOrderChargesBetween(startInstant, endInstant);
        BigDecimal discounts = BigDecimal.ZERO;
        BigDecimal taxes = BigDecimal.ZERO;
        BigDecimal shipping = BigDecimal.ZERO;
        if (!charges.isEmpty()) {
            Object[] chargeRow = charges.getFirst();
            discounts = (BigDecimal) chargeRow[0];
            taxes = (BigDecimal) chargeRow[1];
            BigDecimal deliveryCharge = (BigDecimal) chargeRow[2];
            BigDecimal codSurcharge = (BigDecimal) chargeRow[3];
            shipping = deliveryCharge.add(codSurcharge);
        }

        return new RevenueReport(
                startDate, endDate, totalRevenue, grossRevenue, discounts, taxes, shipping,
                dailyRevenue, revenueByPaymentStatus, revenueByPaymentMethod
        );
    }

    @Override
    public ProductReport generateProductReport(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Product performance — DB-aggregated via GROUP BY on OrderItem
        List<ProductPerformanceAggregate> productAggs = orderReadModel
                .aggregateProductPerformanceByDateRange(startInstant, endInstant);

        List<ProductReport.ProductPerformance> productPerformance = productAggs.stream()
                .map(a -> new ProductReport.ProductPerformance(
                        a.getItemId(), a.getItemName(), a.getSku(),
                        a.getQuantitySold(), a.getRevenue(), a.getAveragePrice(), a.getCurrentStock()
                ))
                .toList();

        // Category performance — DB-aggregated via GROUP BY including unique product count
        List<CategorySalesAggregate> categoryAggs = orderReadModel
                .aggregateCategorySalesByDateRange(startInstant, endInstant);

        List<ProductReport.CategoryPerformance> categoryPerformance = categoryAggs.stream()
                .map(a -> new ProductReport.CategoryPerformance(
                        a.getCategoryId(), a.getCategoryName(),
                        a.getQuantitySold(), a.getRevenue(), a.getUniqueProductsSold()
                ))
                .sorted(Comparator.comparing(ProductReport.CategoryPerformance::revenue).reversed())
                .toList();

        long totalProductsSold = productPerformance.stream()
                .mapToLong(ProductReport.ProductPerformance::quantitySold)
                .sum();
        BigDecimal totalProductRevenue = productPerformance.stream()
                .map(ProductReport.ProductPerformance::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProductReport(
                startDate, endDate, totalProductsSold, totalProductRevenue,
                productPerformance, categoryPerformance
        );
    }

    @Override
    public CustomerReport generateCustomerReport(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Customer aggregation — DB GROUP BY with MIN/MAX for first/last order dates
        List<CustomerOrderAggregate> customerAggs = orderReadModel
                .aggregateCustomerOrdersBetween(startInstant, endInstant);

        List<CustomerReport.TopCustomer> topCustomers = customerAggs.stream()
                .limit(20)
                .map(a -> {
                    LocalDate firstOrder = a.getFirstOrderDate() != null
                            ? a.getFirstOrderDate().atZone(ZoneId.systemDefault()).toLocalDate()
                            : LocalDate.now(clock);
                    LocalDate lastOrder = a.getLastOrderDate() != null
                            ? a.getLastOrderDate().atZone(ZoneId.systemDefault()).toLocalDate()
                            : LocalDate.now(clock);
                    return new CustomerReport.TopCustomer(
                            a.getUserId(), a.getUsername(), a.getEmail(),
                            a.getOrderCount(), a.getTotalSpent(), firstOrder, lastOrder
                    );
                })
                .toList();

        // Customer segments — single pass to avoid 6x streaming
        List<CustomerAggData> aggDataList = customerAggs.stream()
                .map(a -> new CustomerAggData(
                        a.getUserId(), a.getUsername(), a.getEmail(),
                        a.getOrderCount(), a.getTotalSpent(),
                        a.getTotalSpent().compareTo(BigDecimal.ZERO) > 0
                                ? a.getTotalSpent().divide(BigDecimal.valueOf(a.getOrderCount()),
                                        2, java.math.RoundingMode.HALF_UP)
                                : BigDecimal.ZERO,
                        a.getFirstOrderDate() != null
                                ? a.getFirstOrderDate().atZone(ZoneId.systemDefault()).toLocalDate()
                                 : LocalDate.now(clock),
                        a.getLastOrderDate() != null
                                ? a.getLastOrderDate().atZone(ZoneId.systemDefault()).toLocalDate()
                                : LocalDate.now(clock)
                ))
                .toList();

        // Single pass: classify each customer into a segment bucket
        Map<String, List<CustomerAggData>> segmentBuckets = new LinkedHashMap<>();
        segmentBuckets.put("VIP", new ArrayList<>());
        segmentBuckets.put("Regular", new ArrayList<>());
        segmentBuckets.put("New", new ArrayList<>());

        for (CustomerAggData d : aggDataList) {
            if (d.totalSpent().compareTo(BigDecimal.valueOf(10000)) > 0) {
                segmentBuckets.get("VIP").add(d);
            } else if (d.totalSpent().compareTo(BigDecimal.valueOf(1000)) > 0) {
                segmentBuckets.get("Regular").add(d);
            } else {
                segmentBuckets.get("New").add(d);
            }
        }

        List<CustomerReport.CustomerSegment> segments = segmentBuckets.entrySet().stream()
                .map(e -> new CustomerReport.CustomerSegment(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .map(CustomerAggData::totalSpent)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .toList();

        long totalCustomers = userReadModel.countAll();
        long newCustomers = userReadModel.countCreatedAfter(startInstant);
        long activeCustomers = customerAggs.size();
        long returningCustomers = activeCustomers - newCustomers;
        BigDecimal totalRevenue = topCustomers.stream()
                .map(CustomerReport.TopCustomer::totalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
        List<ItemDetails> allDetails = productReadModel.findAllItemDetails();

        long totalProducts = productReadModel.countItemDetails();
        int totalStockUnits = allDetails.stream().mapToInt(ItemDetails::getStockQuantity).sum();
        BigDecimal totalInventoryValue = allDetails.stream()
                .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long outOfStockCount = productReadModel.countOutOfStock();
        long lowStockCount = productReadModel.countLowStock(InventoryConstants.LOW_STOCK_THRESHOLD);

        List<InventoryReport.ProductInventoryStatus> productStatuses = allDetails.stream()
                .map(d -> {
                    String status = d.getStockQuantity() == 0 ? "OUT_OF_STOCK"
                            : d.getStockQuantity() <= InventoryConstants.LOW_STOCK_THRESHOLD ? "LOW_STOCK" : "IN_STOCK";
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
                 LocalDate.now(clock), totalProducts, totalStockUnits, totalInventoryValue,
                lowStockCount, outOfStockCount, productStatuses
        );
    }

    @Override
    public List<CommissionReportResponse> generateCommissionReport() {
        List<SellerRevenueAggregate> results = orderReadModel.aggregateRevenueBySeller(PaymentStatus.PAID);
        List<CommissionReportResponse> reports = new ArrayList<>();

        for (SellerRevenueAggregate row : results) {
            Long sellerId = row.getSellerId();
            String businessName = row.getBusinessName();
            long totalOrders = row.getTotalOrders();
            BigDecimal totalRevenue = row.getTotalRevenue();

            var seller = productReadModel.findSellerById(sellerId)
                    .orElseThrow(() -> SellerNotFoundException.id(sellerId));
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
        return switch (format) {
            case CSV -> generateCsv(reportType, startDate, endDate);
            case PDF, EXCEL -> throw new UnsupportedOperationException(
                    format + " export is not yet implemented. Use CSV instead.");
        };
    }

    // ── CSV generation ──────────────────────────────────────────────────

    private byte[] generateCsv(ReportType reportType, LocalDate startDate, LocalDate endDate) {
        String csv = switch (reportType) {
            case SALES -> salesReportToCsv(generateSalesReport(startDate, endDate));
            case REVENUE -> revenueReportToCsv(generateRevenueReport(startDate, endDate));
            case PRODUCT -> productReportToCsv(generateProductReport(startDate, endDate));
            case CUSTOMER -> customerReportToCsv(generateCustomerReport(startDate, endDate));
            case INVENTORY -> inventoryReportToCsv(generateInventoryReport());
        };
        return csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String salesReportToCsv(SalesReport report) {
        StringBuilder sb = new StringBuilder();

        // Summary
        sb.append("Sales Report\n");
        sb.append("Start Date,End Date,Total Orders,Total Revenue,Average Order Value\n");
        sb.append("%s,%s,%d,%s,%s\n\n".formatted(
                report.startDate(), report.endDate(), report.totalOrders(),
                report.totalRevenue(), avgOrderValue(report.totalOrders(), report.totalRevenue())));

        // Daily sales
        sb.append("Daily Sales\n");
        sb.append("Date,Orders,Revenue\n");
        for (var entry : report.dailySales().entrySet()) {
            sb.append("%s,%d,%s\n".formatted(entry.getKey(), entry.getValue().orders(), entry.getValue().revenue()));
        }
        sb.append("\n");

        // Top products
        sb.append("Top Products\n");
        sb.append("Item ID,Name,SKU,Quantity Sold,Revenue\n");
        for (var p : report.topProducts()) {
            sb.append("%d,\"%s\",%s,%d,%s\n".formatted(
                    p.itemId(), escapeCsv(p.itemName()), p.sku(), p.quantitySold(), p.revenue()));
        }
        sb.append("\n");

        // Top categories
        sb.append("Top Categories\n");
        sb.append("Category ID,Name,Quantity Sold,Revenue\n");
        for (var c : report.topCategories()) {
            sb.append("%d,\"%s\",%d,%s\n".formatted(
                    c.categoryId(), escapeCsv(c.categoryName()), c.quantitySold(), c.revenue()));
        }

        return sb.toString();
    }

    private String revenueReportToCsv(RevenueReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("Revenue Report\n");
        sb.append("Start Date,End Date,Total Revenue,Gross Revenue,Discounts,Taxes,Shipping\n");
        sb.append("%s,%s,%s,%s,%s,%s,%s\n\n".formatted(
                report.startDate(), report.endDate(), report.totalRevenue(),
                report.grossRevenue(), report.discounts(), report.taxes(), report.shipping()));

        // Daily revenue
        sb.append("Daily Revenue\n");
        sb.append("Date,Revenue\n");
        for (var entry : report.dailyRevenue().entrySet()) {
            sb.append("%s,%s\n".formatted(entry.getKey(), entry.getValue()));
        }
        sb.append("\n");

        // Revenue by payment method
        sb.append("Revenue by Payment Method\n");
        sb.append("Method,Revenue\n");
        for (var entry : report.revenueByPaymentMethod().entrySet()) {
            sb.append("%s,%s\n".formatted(entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }

    private String productReportToCsv(ProductReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("Product Report\n");
        sb.append("Start Date,End Date,Total Products Sold,Total Revenue\n");
        sb.append("%s,%s,%d,%s\n\n".formatted(
                report.startDate(), report.endDate(),
                report.totalProductsSold(), report.totalProductRevenue()));

        // Product performance
        sb.append("Product Performance\n");
        sb.append("Item ID,Name,SKU,Quantity Sold,Revenue,Average Price,Current Stock\n");
        for (var p : report.productPerformance()) {
            sb.append("%d,\"%s\",%s,%d,%s,%s,%d\n".formatted(
                    p.itemId(), escapeCsv(p.itemName()), p.sku(),
                    p.quantitySold(), p.revenue(), p.averagePrice(), p.currentStock()));
        }
        sb.append("\n");

        // Category performance
        sb.append("Category Performance\n");
        sb.append("Category ID,Name,Quantity Sold,Revenue,Unique Products Sold\n");
        for (var c : report.categoryPerformance()) {
            sb.append("%d,\"%s\",%d,%s,%d\n".formatted(
                    c.categoryId(), escapeCsv(c.categoryName()),
                    c.quantitySold(), c.revenue(), c.uniqueProductsSold()));
        }

        return sb.toString();
    }

    private String customerReportToCsv(CustomerReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("Customer Report\n");
        sb.append("Start Date,End Date,Total Customers,New Customers,Returning Customers,Total Revenue,Average Order Value\n");
        sb.append("%s,%s,%d,%d,%d,%s,%s\n\n".formatted(
                report.startDate(), report.endDate(), report.totalCustomers(),
                report.newCustomers(), report.returningCustomers(),
                report.totalRevenue(), report.averageOrderValue()));

        // Segments
        sb.append("Customer Segments\n");
        sb.append("Segment,Count,Revenue\n");
        for (var s : report.customerSegments()) {
            sb.append("%s,%d,%s\n".formatted(s.segment(), s.count(), s.revenue()));
        }
        sb.append("\n");

        // Top customers
        sb.append("Top Customers\n");
        sb.append("User ID,Username,Email,Order Count,Total Spent,First Order,Last Order\n");
        for (var c : report.topCustomers()) {
            sb.append("%d,\"%s\",\"%s\",%d,%s,%s,%s\n".formatted(
                    c.userId(), escapeCsv(c.username()), escapeCsv(c.email()),
                    c.orderCount(), c.totalSpent(), c.firstOrderDate(), c.lastOrderDate()));
        }

        return sb.toString();
    }

    private String inventoryReportToCsv(InventoryReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("Inventory Report\n");
        sb.append("Report Date,Total Products,Total Stock Units,Total Inventory Value,Low Stock,Out of Stock\n");
        sb.append("%s,%d,%d,%s,%d,%d\n\n".formatted(
                report.reportDate(), report.totalProducts(), report.totalStockUnits(),
                report.totalInventoryValue(), report.lowStockProducts(), report.outOfStockProducts()));

        // Product statuses
        sb.append("Product Inventory Status\n");
        sb.append("Item ID,Name,SKU,Category,Stock Quantity,Unit Cost,Inventory Value,Status\n");
        for (var p : report.productStatuses()) {
            sb.append("%d,\"%s\",%s,\"%s\",%d,%s,%s,%s\n".formatted(
                    p.itemId(), escapeCsv(p.itemName()), p.sku(),
                    escapeCsv(p.category()), p.stockQuantity(),
                    p.unitCost(), p.inventoryValue(), p.stockStatus()));
        }

        return sb.toString();
    }

    // ── CSV helpers ──────────────────────────────────────────────────────

    private static String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private static BigDecimal avgOrderValue(long orders, BigDecimal revenue) {
        return orders > 0
                ? revenue.divide(BigDecimal.valueOf(orders), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

}
