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
 * Persistence operations for the {@link Payment} aggregate.
 *
 * <p>Provides methods to query payments by order, transaction ID, status, and date range for
 * payment management and reporting. The repository supports paginated queries for payment
 * listing, aggregate queries for revenue analytics, and batch operations for user payment
 * history.</p>
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

    /**
     * Returns the 10 most recent payments with order and user eagerly fetched.
     *
     * @return list of recent payments
     */
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

    /**
     * Sums payment amounts for a specific user by status.
     *
     * @param user   the user whose payments to sum
     * @param status the payment status
     * @return total amount
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.user = :user AND p.paymentStatus = :status")
    BigDecimal sumAmountByUserAndStatus(@Param("user") User user, @Param("status") PaymentStatus status);

    /**
     * Batch-sums payment amounts per user — avoids N+1 when enriching a list of
     * users with their total spent (BUG-003).
     *
     * <p><strong>Callers must guard against empty {@code userIds} lists</strong>;
     * JPQL {@code IN :userIds} with an empty collection produces undefined
     * behaviour across JPA providers. The service layer
     * ({@code PaymentLookupServiceImpl.sumPaidAmountByUserIds}) already
     * short-circuits with {@code Map.of()} for empty input.</p>
     *
     * @param userIds the user IDs to sum amounts for
     * @param status  the payment status to filter by
     * @return list of {@code [userId, sum]} tuples
     */
    @Query("SELECT p.order.user.id, COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.user.id IN :userIds AND p.paymentStatus = :status GROUP BY p.order.user.id")
    List<Object[]> sumAmountByUserIdsAndStatus(@Param("userIds") List<Long> userIds, @Param("status") PaymentStatus status);

    /**
     * Checks whether a payment record exists for the given order.
     *
     * @param order the order to check
     * @return true if a payment exists
     */
    boolean existsByOrder(Order order);

    /**
     * Returns payments within a date range filtered by status, ordered by paid date ascending.
     *
     * @param status the payment status
     * @param start  start of date range (inclusive)
     * @param end    end of date range (exclusive)
     * @return list of matching payments
     */
    List<Payment> findByPaymentStatusAndPaidAtBetweenOrderByPaidAtAsc(PaymentStatus status, Instant start, Instant end);

    /**
     * Returns payments within a date range filtered by a list of statuses.
     *
     * @param start    start of date range (inclusive)
     * @param end      end of date range (exclusive)
     * @param statuses list of payment statuses to match
     * @return list of matching payments
     */
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
            SELECT FUNCTION('DATE', COALESCE(p.paidAt, p.createdAt)), COALESCE(SUM(p.amount), 0)
            FROM Payment p WHERE p.paymentStatus = 'PAID'
            AND COALESCE(p.paidAt, p.createdAt) BETWEEN :start AND :end
            GROUP BY FUNCTION('DATE', COALESCE(p.paidAt, p.createdAt))
            ORDER BY FUNCTION('DATE', COALESCE(p.paidAt, p.createdAt)) ASC""")
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
