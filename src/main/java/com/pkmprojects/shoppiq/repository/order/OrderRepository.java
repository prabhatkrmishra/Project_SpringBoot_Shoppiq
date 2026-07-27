package com.pkmprojects.shoppiq.repository.order;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Order} persistence.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserOrderByPlacedAtDesc(User user);

    Page<Order> findAllByUserOrderByPlacedAtDesc(User user, Pageable pageable);

    Page<Order> findAll(Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Optional<Order> findById(Long id);

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

    List<Order> findTop10ByOrderByPlacedAtDesc();

    long countByStatus(OrderStatus status);

    long countByPlacedAtBetween(Instant start, Instant end);

    List<Order> findByPlacedAtBetween(Instant start, Instant end);

    List<Order> findByPlacedAtBetweenOrderByPlacedAtAsc(Instant start, Instant end);

    long countByUser(User user);

    /**
     * Finds all distinct orders that contain items belonging to a specific seller.
     *
     * @param sellerId the seller identifier
     * @return list of orders containing the seller's products
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.itemDetails id JOIN id.item i JOIN i.seller s WHERE s.id = :sellerId ORDER BY o.placedAt DESC")
    List<Order> findDistinctBySellerIdOrderByPlacedAtDesc(@Param("sellerId") Long sellerId);

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
}
