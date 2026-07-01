package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.OrderItem;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for {@link OrderItem} persistence.
 *
 * @author PrabhatKrMishra
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
     * @return list of [sellerId, businessName, totalOrders, totalRevenue]
     */
    @Query("SELECT s.id, s.businessName, COUNT(DISTINCT o.id), COALESCE(SUM(oi.subtotal), 0) FROM OrderItem oi JOIN oi.itemDetails id JOIN id.item i JOIN i.seller s JOIN oi.order o WHERE o.paymentStatus = :paymentStatus GROUP BY s.id, s.businessName")
    List<Object[]> aggregateRevenueBySeller(@Param("paymentStatus") PaymentStatus paymentStatus);
}
