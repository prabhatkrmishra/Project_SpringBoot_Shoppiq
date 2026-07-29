package com.pkmprojects.shoppiq.repository.payment;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link Payment} persistence.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived association queries</strong> — {@code findByOrder(Order)} and
 *       {@code findByTransactionId} show lookups by both entity association and flat field.</li>
 *   <li><strong>{@code @EntityGraph}</strong> — {@code findTop10ByOrderByCreatedAtDesc} eagerly
 *       fetches {@code order} and {@code order.user} to avoid N+1 queries in the admin dashboard.</li>
 *   <li><strong>Derived queries with pagination</strong> — {@code findByPaymentStatus} with
 *       {@link org.springframework.data.domain.Pageable} demonstrates enum-based filtering
 *       with automatic count and pagination.</li>
 *   <li><strong>JPQL aggregation with COALESCE</strong> — {@code sumAmountByPaymentStatus},
 *       {@code sumAmountByStatusAndDateRange}, and {@code sumAmountByUserAndStatus} use
 *       {@code COALESCE(SUM(p.amount), 0)} for safe numeric aggregation returning
 *       {@link java.math.BigDecimal}.</li>
 *   <li><strong>Derived exists query</strong> — {@code existsByOrder} checks for existing
 *       payment records without loading the full entity.</li>
 *   <li><strong>Combined derived queries</strong> — {@code findByPaymentStatusAndPaidAtBetweenOrderByPaidAtAsc}
 *       chains status filter, date range, and ordering:
 *       {@code WHERE payment_status = ? AND paid_at BETWEEN ? AND ? ORDER BY paid_at ASC}.</li>
 *   <li><strong>IN-clause with between</strong> — {@code findByCreatedAtBetweenAndPaymentStatusIn}
 *       combines date range with a list of statuses.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findByOrder(Order)
 *       → SELECT * FROM payments WHERE order_id = ?
 *   findByTransactionId(String)
 *       → SELECT * FROM payments WHERE transaction_id = ?
 *   findByPaymentStatus(PaymentStatus, Pageable)
 *       → SELECT * FROM payments WHERE payment_status = ? LIMIT ? OFFSET ?
 *   countByPaymentStatus(PaymentStatus)
 *       → SELECT COUNT(*) FROM payments WHERE payment_status = ?
 *   findTop10ByOrderByCreatedAtDesc
 *       → SELECT * FROM payments ORDER BY created_at DESC LIMIT 10
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds the payment record associated with the given order.
     *
     * @param order the parent order
     * @return optional payment
     */
    Optional<Payment> findByOrder(Order order);

    /**
     * Finds a payment by the external transaction ID returned by the gateway.
     *
     * @param transactionId gateway transaction ID
     * @return optional payment
     */
    Optional<Payment> findByTransactionId(String transactionId);

    @EntityGraph(attributePaths = {"order", "order.user"})
    List<Payment> findTop10ByOrderByCreatedAtDesc();

    /**
     * Returns a paginated view of all payments filtered by status.
     *
     * @param status   payment status
     * @param pageable pagination params
     * @return page of payments
     */
    Page<Payment> findByPaymentStatus(PaymentStatus status, Pageable pageable);

    /**
     * Counts payments by status.
     *
     * @param status payment status
     * @return count of payments
     */
    long countByPaymentStatus(PaymentStatus status);

    /**
     * Sums payment amounts by status.
     *
     * @param status payment status
     * @return total amount or null
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status")
    BigDecimal sumAmountByPaymentStatus(@Param("status") PaymentStatus status);

    /**
     * Sums payment amounts by status within a date range.
     *
     * @param status payment status
     * @param start  start instant
     * @param end    end instant
     * @return total amount or null
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.paidAt BETWEEN :start AND :end")
    BigDecimal sumAmountByStatusAndDateRange(@Param("status") PaymentStatus status, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.user = :user AND p.paymentStatus = :status")
    BigDecimal sumAmountByUserAndStatus(@Param("user") User user, @Param("status") PaymentStatus status);

    /**
     * Batch-sums payment amounts per user — avoids N+1 when enriching a list of
     * users with their total spent (BUG-003).
     *
     * @param userIds the user IDs to sum amounts for
     * @param status  the payment status to filter by
     * @return list of {@code [userId, sum]} tuples
     */
    @Query("SELECT p.order.user.id, COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.user.id IN :userIds AND p.paymentStatus = :status GROUP BY p.order.user.id")
    List<Object[]> sumAmountByUserIdsAndStatus(@Param("userIds") List<Long> userIds, @Param("status") PaymentStatus status);

    boolean existsByOrder(Order order);

    List<Payment> findByPaymentStatusAndPaidAtBetweenOrderByPaidAtAsc(PaymentStatus status, Instant start, Instant end);

    List<Payment> findByCreatedAtBetweenAndPaymentStatusIn(Instant start, Instant end, List<PaymentStatus> statuses);

    // =========================================================
    // BUG-008: Optimized aggregate report queries
    // =========================================================

    /**
     * Aggregates daily revenue from PAID payments within a date range.
     * Uses {@code paidAt} when available, falling back to {@code createdAt}.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive with +1 day added by caller)
     * @return list of [paymentDate, dailyTotal] tuples
     */
    @Query("""
            SELECT COALESCE(p.paidAt, p.createdAt), COALESCE(SUM(p.amount), 0)
            FROM Payment p WHERE p.paymentStatus = 'PAID'
            AND COALESCE(p.paidAt, p.createdAt) BETWEEN :start AND :end
            GROUP BY COALESCE(p.paidAt, p.createdAt)
            ORDER BY COALESCE(p.paidAt, p.createdAt) ASC""")
    List<Object[]> aggregateDailyRevenueBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Aggregates revenue by payment method for PAID payments within a date range.
     *
     * @param start start of date range (inclusive)
     * @param end   end of date range (exclusive with +1 day added by caller)
     * @return list of [paymentMethod, totalAmount] tuples
     */
    @Query("""
            SELECT p.paymentMethod, COALESCE(SUM(p.amount), 0)
            FROM Payment p WHERE p.paymentStatus = 'PAID'
            AND COALESCE(p.paidAt, p.createdAt) BETWEEN :start AND :end
            GROUP BY p.paymentMethod""")
    List<Object[]> aggregateRevenueByPaymentMethodBetween(@Param("start") Instant start, @Param("end") Instant end);
}
