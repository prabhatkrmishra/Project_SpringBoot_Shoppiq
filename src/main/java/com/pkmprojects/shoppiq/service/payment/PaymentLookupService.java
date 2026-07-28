package com.pkmprojects.shoppiq.service.payment;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <strong>Spring Boot Concept:</strong> Read-only payment query facade.
 *
 * <p>Decouples service-layer code from {@code PaymentRepository},
 * providing payment lookup, aggregate, and paging queries.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface PaymentLookupService {

    /**
     * Finds a payment by primary key.
     */
    Optional<Payment> findById(Long paymentId);

    /**
     * Returns true if a payment exists for the given order.
     */
    boolean existsByOrder(Order order);

    /**
     * Returns total payment count.
     */
    long count();

    /**
     * Returns payment count by status.
     */
    long countByStatus(PaymentStatus status);

    /**
     * Sums payment amounts by status.
     */
    BigDecimal sumAmountByStatus(PaymentStatus status);

    /**
     * Sums a user's total paid amount.
     */
    BigDecimal sumAmountByUserAndStatus(User user, PaymentStatus status);

    /**
     * Returns paginated payments filtered by status.
     */
    Page<Payment> findByStatus(PaymentStatus status, int page, int size);

    /**
     * Returns paginated all payments.
     */
    Page<Payment> findAll(int page, int size);

    /**
     * Sums payment amounts by status within a time range.
     */
    BigDecimal sumAmountByStatusAndDateRange(PaymentStatus status, Instant start, Instant end);

    /**
     * Returns paid payments within the given time range, ordered by paidAt ascending.
     */
    List<Payment> findPaidBetweenAsc(Instant start, Instant end);

    /**
     * Returns payments within the time range matching any of the given statuses.
     */
    List<Payment> findByDateRangeAndStatuses(Instant start, Instant end, List<PaymentStatus> statuses);

    /**
     * Returns the 10 most recently created payments.
     */
    List<Payment> findRecentTop10();

    /**
     * Batch-sums paid amounts for the given user IDs — avoids N+1
     * when enriching a list of users with their total spent (BUG-003).
     *
     * @param userIds the user IDs to sum paid amounts for
     * @return map of userId → total paid amount
     */
    Map<Long, BigDecimal> sumPaidAmountByUserIds(List<Long> userIds);

    /**
     * Returns [paidAt/createdAt, amount] tuples for PAID payments within date range.
     */
    List<Object[]> aggregateDailyRevenueBetween(Instant start, Instant end);

    /**
     * Returns [paymentMethod, amount] tuples for PAID payments within date range.
     */
    List<Object[]> aggregateRevenueByPaymentMethodBetween(Instant start, Instant end);
}
