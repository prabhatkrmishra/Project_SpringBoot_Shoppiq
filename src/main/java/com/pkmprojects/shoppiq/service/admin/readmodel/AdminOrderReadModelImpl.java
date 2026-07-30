package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.repository.order.OrderItemRepository;
import com.pkmprojects.shoppiq.repository.order.OrderRepository;
import com.pkmprojects.shoppiq.repository.order.projection.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * {@link AdminOrderReadModel} implementation providing read-only order queries
 * for admin dashboards and reports.
 *
 * @author prabhatkrmishra
 * @see AdminOrderReadModel
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AdminOrderReadModelImpl implements AdminOrderReadModel {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Returns the total number of orders.
     *
     * @return total order count
     */
    @Override
    public long countAll() {
        return orderRepository.count();
    }

    /**
     * Counts orders by their current status.
     *
     * @param status the order status to count
     * @return count of orders with the given status
     */
    @Override
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Counts orders placed within a date range.
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return count of orders placed in the range
     */
    @Override
    public long countPlacedBetween(Instant start, Instant end) {
        return orderRepository.countByPlacedAtBetween(start, end);
    }

    /**
     * Counts orders placed within a date range with a specific status.
     *
     * @param start  start of the date range (inclusive)
     * @param end    end of the date range (inclusive)
     * @param status the order status to filter by
     * @return count of orders
     */
    @Override
    public long countPlacedBetween(Instant start, Instant end, OrderStatus status) {
        return orderRepository.countByPlacedAtBetweenAndStatus(start, end, status);
    }

    /**
     * Finds orders placed within a date range, ascending by placed-at timestamp.
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of orders in ascending order
     */
    @Override
    public List<Order> findPlacedBetweenAsc(Instant start, Instant end, Pageable pageable) {
        return orderRepository.findByPlacedAtBetweenOrderByPlacedAtAsc(start, end, pageable).getContent();
    }

    /**
     * Finds orders placed within a date range with a specific status, ascending.
     *
     * @param start  start of the date range (inclusive)
     * @param end    end of the date range (inclusive)
     * @param status the order status to filter by
     * @return list of matching orders in ascending order
     */
    @Override
    public List<Order> findPlacedBetweenAsc(Instant start, Instant end, OrderStatus status, Pageable pageable) {
        return orderRepository.findByPlacedAtBetweenAndStatusOrderByPlacedAtAsc(start, end, status, pageable).getContent();
    }

    /**
     * Retrieves the 10 most recently placed orders.
     *
     * @return list of the 10 most recent orders
     */
    @Override
    public List<Order> findRecentTop10() {
        return orderRepository.findTop10ByOrderByPlacedAtDesc();
    }

    /**
     * Aggregates revenue by seller for payments with the given status.
     *
     * @param paymentStatus the payment status to filter by (e.g. PAID)
     * @return list of seller revenue aggregates
     */
    @Override
    public List<SellerRevenueAggregate> aggregateRevenueBySeller(PaymentStatus paymentStatus) {
        return orderItemRepository.aggregateRevenueBySeller(paymentStatus);
    }

    /**
     * Returns placedAt and grandTotal for orders within a date range.
     *
     * <p>Returns [placedAt, grandTotal] tuples for efficient grouping in service layer,
     * avoiding the full entity graph load.</p>
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of [placedAt, grandTotal] tuples
     */
    @Override
    public List<Object[]> findOrderValuesBetween(Instant start, Instant end) {
        return orderRepository.findOrderValuesBetween(start, end);
    }

    /**
     * Returns placedAt, grandTotal, and status for orders within a date range.
     *
     * <p>Returns [placedAt, grandTotal, status] tuples for efficient grouping in service layer,
     * avoiding the full entity graph load.</p>
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of [placedAt, grandTotal, status] tuples
     */
    @Override
    public List<Object[]> findOrderValuesAndStatusBetween(Instant start, Instant end) {
        return orderRepository.findOrderValuesAndStatusBetween(start, end);
    }

    /**
     * Aggregates order-level charges (discounts, taxes, delivery, COD surcharge) within a date range.
     *
     * <p>Returns a single [totalDiscount, totalTax, totalDeliveryCharge, totalCodSurcharge] tuple
     * for efficient revenue reporting.</p>
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list containing a single tuple of aggregated charges
     */
    @Override
    public List<Object[]> aggregateOrderChargesBetween(Instant start, Instant end) {
        return orderRepository.aggregateOrderChargesBetween(start, end);
    }

    /**
     * Aggregates per-customer order statistics within a date range.
     *
     * <p>Returns customer aggregates with order count, total spent, and first/last order dates
     * computed directly in the database using GROUP BY.</p>
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of customer order aggregates
     */
    @Override
    public List<CustomerOrderAggregate> aggregateCustomerOrdersBetween(Instant start, Instant end) {
        return orderRepository.aggregateCustomerOrdersBetween(start, end);
    }

    /**
     * Aggregates sales per product within a date range.
     *
     * <p>Returns product aggregates with quantity sold and revenue, ordered by revenue descending.
     * Computed directly in the database using GROUP BY on OrderItem.</p>
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of product sales aggregates
     */
    @Override
    public List<ProductSalesAggregate> aggregateProductSalesByDateRange(Instant start, Instant end) {
        return orderItemRepository.aggregateProductSalesByDateRange(start, end);
    }

    /**
     * Aggregates sales per category within a date range.
     *
     * <p>Returns category aggregates with quantity sold, revenue, and unique products sold.
     * Computed directly in the database using GROUP BY on OrderItem.</p>
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of category sales aggregates
     */
    @Override
    public List<CategorySalesAggregate> aggregateCategorySalesByDateRange(Instant start, Instant end) {
        return orderItemRepository.aggregateCategorySalesByDateRange(start, end);
    }

    /**
     * Aggregates product performance metrics within a date range.
     *
     * <p>Returns product aggregates with quantity sold, revenue, average price, and current stock.
     * Computed directly in the database using GROUP BY on OrderItem.</p>
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of product performance aggregates
     */
    @Override
    public List<ProductPerformanceAggregate> aggregateProductPerformanceByDateRange(Instant start, Instant end) {
        return orderItemRepository.aggregateProductPerformanceByDateRange(start, end);
    }
}
