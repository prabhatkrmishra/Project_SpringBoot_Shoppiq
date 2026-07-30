package com.pkmprojects.shoppiq.repository.order;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.repository.order.projection.CustomerOrderAggregate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for the {@link Order} aggregate.
 *
 * <p>Provides methods to query orders by user, status, date range, and seller for order management
 * and reporting. The repository supports paginated queries for order listing, eager fetching of
 * order items and associations, and aggregate queries for sales analytics and customer reporting.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Returns a paginated view of orders for a user, with order items and product details eagerly fetched.
     *
     * @param user     the order owner
     * @param pageable pagination parameters
     * @return paginated list of orders
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.itemDetails", "orderItems.itemDetails.item"})
    Page<Order> findAllByUserOrderByPlacedAtDesc(User user, Pageable pageable);

    /**
     * Returns a paginated view of all orders.
     *
     * @param pageable pagination parameters
     * @return paginated list of orders
     */
    Page<Order> findAll(Pageable pageable);

    /**
     * Returns a paginated view of orders filtered by status.
     *
     * @param status   the order status
     * @param pageable pagination parameters
     * @return paginated list of matching orders
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Finds an order by its identifier.
     *
     * @param id the order identifier
     * @return optional order
     */
    Optional<Order> findById(Long id);

    /**
     * Finds an order by ID with order items and item details eagerly fetched.
     *
     * @param id the order identifier
     * @return the order with items loaded, or empty if not found
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.itemDetails WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    /**
     * Finds an order by ID with the associated {@code User} eagerly fetched.
     *
     * <p>Used by async event listeners that need to access {@code order.getUser()}
     * outside the original persistence context (e.g. email notifications).
     * Without eager fetching, accessing the lazy {@code user} association on a
     * detached entity would throw {@code LazyInitializationException}.</p>
     *
     * @param id the order identifier
     * @return the order with its user association loaded, or empty if not found
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user WHERE o.id = :id")
    Optional<Order> findByIdWithUser(@Param("id") Long id);

    /**
     * Returns the 10 most recently placed orders with user eagerly fetched.
     *
     * @return list of recent orders
     */
    @EntityGraph(attributePaths = "user")
    List<Order> findTop10ByOrderByPlacedAtDesc();

    /**
     * Counts orders by status.
     *
     * @param status the order status
     * @return count of orders with the given status
     */
    long countByStatus(OrderStatus status);

    /**
     * Counts orders placed within a date range.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive)
     * @return count of orders
     */
    long countByPlacedAtBetween(Instant start, Instant end);

    /**
     * Counts orders placed within a date range with a specific status.
     */
    long countByPlacedAtBetweenAndStatus(Instant start, Instant end, OrderStatus status);

    /**
     * Returns a paginated view of orders within a date range and status, with order items eagerly fetched.
     *
     * @param start    start of date range (inclusive)
     * @param end      end of date range (exclusive)
     * @param status   the order status
     * @param pageable pagination parameters
     * @return paginated list of matching orders
     */
    @EntityGraph(attributePaths = {
            "orderItems",
            "orderItems.itemDetails",
            "orderItems.itemDetails.category"
    })
    Page<Order> findByPlacedAtBetweenAndStatusOrderByPlacedAtAsc(Instant start, Instant end, OrderStatus status, Pageable pageable);

    /**
     * Returns a paginated view of orders within a date range, with full entity graph eagerly fetched.
     *
     * @param start    start of date range (inclusive)
     * @param end      end of date range (exclusive)
     * @param pageable pagination parameters
     * @return paginated list of matching orders
     */
    @EntityGraph(attributePaths = {
            "orderItems",
            "orderItems.itemDetails",
            "orderItems.itemDetails.category",
            "orderItems.itemDetails.item",
            "user"
    })
    Page<Order> findByPlacedAtBetweenOrderByPlacedAtAsc(Instant start, Instant end, Pageable pageable);

    /**
     * Counts orders for a given user.
     *
     * @param user the order owner
     * @return count of orders
     */
    long countByUser(User user);

    /**
     * Batch-counts orders per user — avoids N+1 when enriching a list of users
     * with their order counts (BUG-003).
     *
     * @param userIds the user IDs to count orders for
     * @return list of {@code [userId, count]} tuples
     */
    @Query("SELECT o.user.id, COUNT(o) FROM Order o WHERE o.user.id IN :userIds GROUP BY o.user.id")
    List<Object[]> countByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Finds all distinct orders that contain items belonging to a specific seller.
     *
     * @param sellerId the seller identifier
     * @return list of orders containing the seller's products
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.itemDetails id JOIN id.item i JOIN i.seller s WHERE s.id = :sellerId ORDER BY o.placedAt DESC")
    @EntityGraph(attributePaths = {
            "orderItems",
            "orderItems.itemDetails",
            "orderItems.itemDetails.item",
            "orderItems.itemDetails.item.seller"
    })
    List<Order> findDistinctBySellerIdOrderByPlacedAtDesc(@Param("sellerId") Long sellerId);

    /**
     * Returns a paginated view of distinct orders containing items belonging to a specific seller.
     *
     * @param sellerId the seller identifier
     * @param pageable pagination parameters
     * @return paginated list of orders containing the seller's products
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.itemDetails id JOIN id.item i JOIN i.seller s WHERE s.id = :sellerId ORDER BY o.placedAt DESC")
    Page<Order> findDistinctBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    /**
     * Counts the distinct orders that contain items belonging to a specific seller.
     *
     * @param sellerId the seller identifier
     * @return count of distinct orders containing the seller's products
     */
    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderItems oi JOIN oi.itemDetails id JOIN id.item i JOIN i.seller s WHERE s.id = :sellerId")
    long countDistinctBySellerId(@Param("sellerId") Long sellerId);

    /**
     * Counts how many items in an order belong to a specific seller.
     *
     * @param orderId  the order identifier
     * @param sellerId the seller identifier
     * @return count of items belonging to the seller
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi JOIN oi.itemDetails id JOIN id.item i JOIN i.seller s WHERE oi.order.id = :orderId AND s.id = :sellerId")
    long countSellerItemsInOrder(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);

    /**
     * Counts the total number of items in an order.
     *
     * @param orderId the order identifier
     * @return total item count in the order
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.id = :orderId")
    long countTotalItemsInOrder(@Param("orderId") Long orderId);

    // =========================================================
    // Report aggregation queries (BUG-008)
    // =========================================================

    /**
     * Returns order timestamp and grand total for all orders within a date range,
     * ordered ascending. Avoids loading the full entity graph that includes
     * {@code orderItems}, {@code itemDetails}, {@code category}, and {@code user}.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive, with +1 day added by caller)
     * @return list of [placedAt, grandTotal] tuples
     */
    @Query("SELECT o.placedAt, o.grandTotal FROM Order o WHERE o.placedAt BETWEEN :start AND :end ORDER BY o.placedAt ASC")
    List<Object[]> findOrderValuesBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Returns order timestamp, grand total, and status for orders within a date range.
     * Lightweight query that avoids loading the full entity graph.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive, with +1 day added by caller)
     * @return list of [placedAt, grandTotal, status] tuples
     */
    @Query("SELECT o.placedAt, o.grandTotal, o.status FROM Order o WHERE o.placedAt BETWEEN :start AND :end ORDER BY o.placedAt ASC")
    List<Object[]> findOrderValuesAndStatusBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Returns aggregated discount, tax, delivery charge, and COD surcharge
     * for orders within a date range.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive, with +1 day added by caller)
     * @return [totalDiscount, totalTax, totalDeliveryCharge, totalCodSurcharge] or nulls if no orders
     */
    @Query("""
            SELECT COALESCE(SUM(o.discount), 0), COALESCE(SUM(o.tax), 0),
            COALESCE(SUM(o.deliveryCharge), 0), COALESCE(SUM(o.codSurcharge), 0)
            FROM Order o WHERE o.placedAt BETWEEN :start AND :end""")
    List<Object[]> aggregateOrderChargesBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Aggregates per-customer order statistics within a date range,
     * ordering by total spent descending.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive, with +1 day added by caller)
     * @return projections with userId, username, email, orderCount, totalSpent, firstOrderDate, lastOrderDate
     */
    @Query("""
            SELECT o.user.id AS userId, o.user.username AS username, o.user.email AS email,
            COUNT(o) AS orderCount, COALESCE(SUM(o.grandTotal), 0) AS totalSpent,
            MIN(o.placedAt) AS firstOrderDate, MAX(o.placedAt) AS lastOrderDate
            FROM Order o WHERE o.placedAt BETWEEN :start AND :end
            GROUP BY o.user.id, o.user.username, o.user.email
            ORDER BY totalSpent DESC""")
    List<CustomerOrderAggregate> aggregateCustomerOrdersBetween(@Param("start") Instant start,
                                                                @Param("end") Instant end);
}
