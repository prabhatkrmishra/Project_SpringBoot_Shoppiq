package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.repository.order.projection.CategorySalesAggregate;
import com.pkmprojects.shoppiq.repository.order.projection.CustomerOrderAggregate;
import com.pkmprojects.shoppiq.repository.order.projection.ProductPerformanceAggregate;
import com.pkmprojects.shoppiq.repository.order.projection.ProductSalesAggregate;
import com.pkmprojects.shoppiq.repository.order.projection.SellerRevenueAggregate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Read-only order query facade for admin dashboards and reports.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * This interface follows the <strong>ReadModel</strong> pattern — a lightweight facade
 * that decouples admin services from repository interfaces. It sits between the Service layer
 * (e.g., {@code AdminDashboardServiceImpl}) and the Repository layer.
 * </p>
 * <p>
 * By using ReadModels, the admin services do not depend directly on {@code OrderRepository}
 * or {@code OrderItemRepository}, making them easier to test and refactor.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Provide aggregate order queries (counts, filter by date range, recent orders).</li>
 *   <li>Aggregate revenue by seller for commission reports.</li>
 * </ul>
 *
 * <p>Decouples admin services from {@code OrderRepository},
 * {@code OrderItemRepository}, and {@code SellerRepository},
 * providing aggregate queries over order data.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface AdminOrderReadModel {

    /**
     * Returns total order count.
     */
    long countAll();

    /**
     * Returns order count with the given status.
     */
    long countByStatus(OrderStatus status);

    /**
     * Returns order count placed within the given time range.
     */
    long countPlacedBetween(Instant start, Instant end);

    /**
     * Returns order count placed within the given time range with the given status.
     */
    long countPlacedBetween(Instant start, Instant end, OrderStatus status);

    /**
     * Returns orders placed within the given time range, ordered oldest first.
     *
     * @param pageable pagination information
     */
    List<Order> findPlacedBetweenAsc(Instant start, Instant end, Pageable pageable);

    /**
     * Returns orders placed within the given time range with the given status, ordered oldest first.
     *
     * @param pageable pagination information
     */
    List<Order> findPlacedBetweenAsc(Instant start, Instant end, OrderStatus status, Pageable pageable);

    /**
     * Returns the 10 most recently placed orders.
     */
    List<Order> findRecentTop10();

    /**
     * Aggregates revenue and order count by seller for orders
     * with the given payment status.
     *
     * @return typed projections with sellerId, businessName, totalOrders, totalRevenue
     */
    List<SellerRevenueAggregate> aggregateRevenueBySeller(PaymentStatus paymentStatus);

    // =========================================================
    // BUG-008: Optimized aggregate report queries
    // =========================================================

    /**
     * Returns placedAt and grandTotal for orders within a date range,
     * avoiding the full entity graph load.
     *
     * @return list of [placedAt, grandTotal] tuples
     */
    List<Object[]> findOrderValuesBetween(Instant start, Instant end);

    /**
     * Returns placedAt, grandTotal, and status for orders within a date range.
     */
    List<Object[]> findOrderValuesAndStatusBetween(Instant start, Instant end);

    /**
     * Returns aggregated discount, tax, delivery, and COD surcharge for orders.
     *
     * @return list containing a single [totalDiscount, totalTax, totalDeliveryCharge, totalCodSurcharge] tuple
     */
    List<Object[]> aggregateOrderChargesBetween(Instant start, Instant end);

    /**
     * Aggregates per-customer order statistics within a date range.
     */
    List<CustomerOrderAggregate> aggregateCustomerOrdersBetween(Instant start, Instant end);

    /**
     * Aggregates sales per product within a date range, ordered by revenue descending.
     */
    List<ProductSalesAggregate> aggregateProductSalesByDateRange(Instant start, Instant end);

    /**
     * Aggregates sales per category within a date range, ordered by revenue descending.
     */
    List<CategorySalesAggregate> aggregateCategorySalesByDateRange(Instant start, Instant end);

    /**
     * Aggregates product performance (quantity, revenue, avg price) within a date range.
     */
    List<ProductPerformanceAggregate> aggregateProductPerformanceByDateRange(Instant start, Instant end);
}
