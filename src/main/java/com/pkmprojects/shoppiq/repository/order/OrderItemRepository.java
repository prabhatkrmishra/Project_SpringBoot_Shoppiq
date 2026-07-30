package com.pkmprojects.shoppiq.repository.order;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.order.OrderItem;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.repository.order.projection.CategorySalesAggregate;
import com.pkmprojects.shoppiq.repository.order.projection.ProductPerformanceAggregate;
import com.pkmprojects.shoppiq.repository.order.projection.ProductSalesAggregate;
import com.pkmprojects.shoppiq.repository.order.projection.SellerRevenueAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Persistence operations for the {@link OrderItem} aggregate.
 *
 * <p>Provides methods to query order items by order, seller, and date range for order management
 * and sales reporting. The repository supports aggregate queries for product performance,
 * category sales, and seller revenue analytics.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Returns all line items for the given order.
     *
     * @param order the parent order
     * @return list of order items
     */
    List<OrderItem> findAllByOrder(Order order);

    /**
     * Sums the subtotal of all line items belonging to a seller
     * in orders with a given payment status.
     *
     * @param sellerId      the seller identifier
     * @param paymentStatus the payment status to filter by
     * @return total revenue from seller's items
     */
    @Query("SELECT COALESCE(SUM(oi.subtotal), 0) FROM OrderItem oi JOIN oi.itemDetails id JOIN id.item i JOIN i.seller s JOIN oi.order o WHERE s.id = :sellerId AND o.paymentStatus = :paymentStatus")
    BigDecimal sumRevenueBySellerIdAndPaymentStatus(@Param("sellerId") Long sellerId, @Param("paymentStatus") PaymentStatus paymentStatus);

    /**
     * Aggregates revenue and order count by seller for paid orders.
     *
     * @param paymentStatus the payment status to filter by
     * @return typed projections with sellerId, businessName, totalOrders, totalRevenue
     */
    @Query("""
            SELECT s.id AS sellerId, s.businessName AS businessName,
            COUNT(DISTINCT o.id) AS totalOrders, COALESCE(SUM(oi.subtotal), 0) AS totalRevenue
            FROM OrderItem oi JOIN oi.itemDetails id JOIN id.item i JOIN i.seller s JOIN oi.order o
            WHERE o.paymentStatus = :paymentStatus GROUP BY s.id, s.businessName""")
    List<SellerRevenueAggregate> aggregateRevenueBySeller(@Param("paymentStatus") PaymentStatus paymentStatus);

    // =========================================================
    // Report aggregation queries (BUG-008)
    // =========================================================

    /**
     * Aggregates sales per product within a date range, ordered by revenue descending.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive with +1 day added by caller)
     * @return projections with itemId, itemName, sku, quantitySold, revenue
     */
    @Query("""
            SELECT oi.itemDetails.id AS itemId, oi.itemNameSnapshot AS itemName,
            oi.itemDetails.sku AS sku,
            COALESCE(SUM(oi.quantity), 0) AS quantitySold,
            COALESCE(SUM(oi.subtotal), 0) AS revenue
            FROM OrderItem oi JOIN oi.order o
            WHERE o.placedAt BETWEEN :start AND :end AND oi.itemDetails IS NOT NULL
            GROUP BY oi.itemDetails.id, oi.itemNameSnapshot, oi.itemDetails.sku
            ORDER BY revenue DESC""")
    List<ProductSalesAggregate> aggregateProductSalesByDateRange(@Param("start") Instant start,
                                                                 @Param("end") Instant end);

    /**
     * Aggregates sales per category within a date range, ordered by revenue descending.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive with +1 day added by caller)
     * @return projections with categoryId, categoryName, quantitySold, revenue
     */
    @Query("""
            SELECT c.id AS categoryId, c.name AS categoryName,
            COALESCE(SUM(oi.quantity), 0) AS quantitySold,
            COALESCE(SUM(oi.subtotal), 0) AS revenue,
            COUNT(DISTINCT oi.itemDetails.id) AS uniqueProductsSold
            FROM OrderItem oi JOIN oi.order o JOIN oi.itemDetails id JOIN id.category c
            WHERE o.placedAt BETWEEN :start AND :end AND oi.itemDetails IS NOT NULL
            GROUP BY c.id, c.name
            ORDER BY revenue DESC""")
    List<CategorySalesAggregate> aggregateCategorySalesByDateRange(@Param("start") Instant start,
                                                                   @Param("end") Instant end);

    /**
     * Aggregates product performance (quantity, revenue, average price) within a date range,
     * joined with current stock from ItemDetails.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive with +1 day added by caller)
     * @return projections with itemId, itemName, sku, quantitySold, revenue, averagePrice, currentStock
     */
    @Query("""
            SELECT oi.itemDetails.id AS itemId, oi.itemNameSnapshot AS itemName,
            oi.itemDetails.sku AS sku,
            COALESCE(SUM(oi.quantity), 0) AS quantitySold,
            COALESCE(SUM(oi.subtotal), 0) AS revenue,
            COALESCE(AVG(oi.unitPriceSnapshot), 0) AS averagePrice,
            id.stockQuantity AS currentStock
            FROM OrderItem oi JOIN oi.order o JOIN oi.itemDetails id
            WHERE o.placedAt BETWEEN :start AND :end AND oi.itemDetails IS NOT NULL
            GROUP BY oi.itemDetails.id, oi.itemNameSnapshot, oi.itemDetails.sku, id.stockQuantity
            ORDER BY revenue DESC""")
    List<ProductPerformanceAggregate> aggregateProductPerformanceByDateRange(@Param("start") Instant start,
                                                                             @Param("end") Instant end);
}
