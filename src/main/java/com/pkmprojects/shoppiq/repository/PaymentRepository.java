package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.Payment;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Payment} persistence.
 *
 * @author PrabhatKrMishra
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
     * Finds a payment by its internal human-readable reference.
     *
     * @param paymentReference e.g. {@code PAY-20260701-42}
     * @return optional payment
     */
    Optional<Payment> findByPaymentReference(String paymentReference);

    /**
     * Finds a payment by the external transaction ID returned by the gateway.
     *
     * @param transactionId gateway transaction ID
     * @return optional payment
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Returns {@code true} if a payment already exists for the given order.
     *
     * @param order the parent order
     * @return {@code true} if a payment record exists
     */
    boolean existsByOrder(Order order);

    /**
     * Returns the 10 most recent payments.
     *
     * @return list of recent payments
     */
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
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.createdAt BETWEEN :start AND :end")
    BigDecimal sumAmountByStatusAndDateRange(@Param("status") PaymentStatus status, @Param("start") Instant start, @Param("end") Instant end);

    /**
     * Finds payments by status within a date range, ordered by paidAt.
     *
     * @param status payment status
     * @param start  start instant
     * @param end    end instant
     * @return list of payments
     */
    List<Payment> findByPaymentStatusAndPaidAtBetweenOrderByPaidAtAsc(PaymentStatus status, Instant start, Instant end);

    /**
     * Sums payment amounts by user and status.
     *
     * @param user   user
     * @param status payment status
     * @return total amount or null
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.user = :user AND p.paymentStatus = :status")
    BigDecimal sumAmountByUserAndStatus(@Param("user") User user, @Param("status") PaymentStatus status);

    /**
     * Finds payments by created date range and statuses.
     *
     * @param start    start instant
     * @param end      end instant
     * @param statuses list of payment statuses
     * @return list of payments
     */
    List<Payment> findByCreatedAtBetweenAndPaymentStatusIn(Instant start, Instant end, List<PaymentStatus> statuses);
}
