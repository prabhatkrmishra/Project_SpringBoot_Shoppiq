package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.time.Instant;
import java.util.List;

/**
 * Read-only order query facade for admin dashboards and reports.
 *
 * <p>Decouples admin services from {@code OrderRepository},
 * {@code OrderItemRepository}, and {@code SellerRepository},
 * providing aggregate queries over order data.</p>
 *
 * @author PrabhatKrMishra
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
     * Returns orders placed within the given time range, ordered oldest first.
     */
    List<Order> findPlacedBetweenAsc(Instant start, Instant end);

    /**
     * Returns the 10 most recently placed orders.
     */
    List<Order> findRecentTop10();

    /**
     * Aggregates revenue and order count by seller for orders
     * with the given payment status.
     *
     * @return list of {@code Object[]{sellerId, businessName, totalOrders, totalRevenue}}
     */
    List<Object[]> aggregateRevenueBySeller(PaymentStatus paymentStatus);
}
