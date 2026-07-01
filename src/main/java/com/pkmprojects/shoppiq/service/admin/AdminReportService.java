package com.pkmprojects.shoppiq.service.admin;

import com.pkmprojects.shoppiq.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Business contract for admin reporting and exports.
 *
 * <p>
 * Defines the operations for generating business reports
 * and exporting data in various formats.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Generate sales report.</li>
 *     <li>Generate revenue report.</li>
 *     <li>Generate product report.</li>
 *     <li>Generate customer report.</li>
 *     <li>Generate inventory report.</li>
 *     <li>Export reports in PDF, Excel, CSV formats.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Works exclusively with DTOs.</li>
 *     <li>Implemented by {@code AdminReportServiceImpl}.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface AdminReportService {

    /**
     * Generates a sales report for the given date range.
     *
     * @param startDate report start date
     * @param endDate   report end date
     * @return sales report data
     */
    SalesReport generateSalesReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generates a revenue report for the given date range.
     *
     * @param startDate report start date
     * @param endDate   report end date
     * @return revenue report data
     */
    RevenueReport generateRevenueReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generates a product performance report.
     *
     * @param startDate report start date
     * @param endDate   report end date
     * @return product report data
     */
    ProductReport generateProductReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generates a customer report.
     *
     * @param startDate report start date
     * @param endDate   report end date
     * @return customer report data
     */
    CustomerReport generateCustomerReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generates an inventory report.
     *
     * @return inventory report data
     */
    InventoryReport generateInventoryReport();

    /**
     * Exports a report to the specified format.
     *
     * @param reportType type of report
     * @param format     export format (PDF, EXCEL, CSV)
     * @param startDate  report start date
     * @param endDate    report end date
     * @return byte array of exported file
     */
    byte[] exportReport(ReportType reportType, ExportFormat format, LocalDate startDate, LocalDate endDate);

    /**
     * Report type enum.
     */
    enum ReportType {
        SALES,
        REVENUE,
        PRODUCT,
        CUSTOMER,
        INVENTORY
    }

    /**
     * Export format enum.
     */
    enum ExportFormat {
        PDF,
        EXCEL,
        CSV
    }

    /**
     * Sales report data.
     */
    record SalesReport(
            LocalDate startDate,
            LocalDate endDate,
            long totalOrders,
            BigDecimal totalRevenue,
            Map<LocalDate, DailySales> dailySales,
            Map<OrderStatus, Long> ordersByStatus,
            List<TopProductSales> topProducts,
            List<TopCategorySales> topCategories
    ) {
        public record DailySales(long orders, BigDecimal revenue) {
        }

        public record TopProductSales(Long itemId, String itemName, String sku, long quantitySold, BigDecimal revenue) {
        }

        public record TopCategorySales(Long categoryId, String categoryName, long quantitySold, BigDecimal revenue) {
        }
    }

    /**
     * Revenue report data.
     */
    record RevenueReport(
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal totalRevenue,
            BigDecimal grossRevenue,
            BigDecimal discounts,
            BigDecimal taxes,
            BigDecimal shipping,
            Map<LocalDate, BigDecimal> dailyRevenue,
            Map<PaymentStatus, BigDecimal> revenueByPaymentStatus,
            Map<String, BigDecimal> revenueByPaymentMethod
    ) {
    }

    /**
     * Product report data.
     */
    record ProductReport(
            LocalDate startDate,
            LocalDate endDate,
            long totalProductsSold,
            BigDecimal totalProductRevenue,
            List<ProductPerformance> productPerformance,
            List<CategoryPerformance> categoryPerformance
    ) {
        public record ProductPerformance(
                Long itemId,
                String itemName,
                String sku,
                long quantitySold,
                BigDecimal revenue,
                BigDecimal averagePrice,
                int currentStock
        ) {
        }

        public record CategoryPerformance(
                Long categoryId,
                String categoryName,
                long quantitySold,
                BigDecimal revenue,
                long uniqueProductsSold
        ) {
        }
    }

    /**
     * Customer report data.
     */
    record CustomerReport(
            LocalDate startDate,
            LocalDate endDate,
            long totalCustomers,
            long newCustomers,
            long returningCustomers,
            BigDecimal totalRevenue,
            BigDecimal averageOrderValue,
            List<CustomerSegment> customerSegments,
            List<TopCustomer> topCustomers
    ) {
        public record CustomerSegment(String segment, long count, BigDecimal revenue) {
        }

        public record TopCustomer(
                Long userId,
                String username,
                String email,
                long orderCount,
                BigDecimal totalSpent,
                LocalDate firstOrderDate,
                LocalDate lastOrderDate
        ) {
        }
    }

    /**
     * Inventory report data.
     */
    record InventoryReport(
            LocalDate reportDate,
            long totalProducts,
            int totalStockUnits,
            BigDecimal totalInventoryValue,
            long lowStockProducts,
            long outOfStockProducts,
            List<ProductInventoryStatus> productStatuses
    ) {
        public record ProductInventoryStatus(
                Long itemId,
                String itemName,
                String sku,
                String category,
                int stockQuantity,
                BigDecimal unitCost,
                BigDecimal inventoryValue,
                String stockStatus
        ) {
        }
    }
}