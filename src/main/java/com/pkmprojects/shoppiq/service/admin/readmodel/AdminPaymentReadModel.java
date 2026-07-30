package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Read-only payment query facade for admin dashboards and reports.
 *
 * <p>Decouples admin services from {@code PaymentRepository},
 * providing aggregate queries over payment data.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface AdminPaymentReadModel {

    /**
     * Sums payment amounts by status.
     */
    BigDecimal sumAmountByStatus(PaymentStatus status);

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
     * Returns [paidAt/createdAt, amount] tuples for PAID payments within date range.
     */
    List<Object[]> aggregateDailyRevenueBetween(Instant start, Instant end);

    /**
     * Returns [paymentMethod, amount] tuples for PAID payments within date range.
     */
    List<Object[]> aggregateRevenueByPaymentMethodBetween(Instant start, Instant end);
}
