package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.service.payment.PaymentLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * {@link AdminPaymentReadModel} implementation providing read-only payment queries
 * for admin dashboards and reports.
 *
 * @author prabhatkrmishra
 * @see AdminPaymentReadModel
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AdminPaymentReadModelImpl implements AdminPaymentReadModel {

    private final PaymentLookupService paymentLookupService;

    /**
     * Sums payment amounts by status.
     *
     * @param status the payment status to filter by
     * @return total sum of payment amounts
     */
    @Override
    public BigDecimal sumAmountByStatus(PaymentStatus status) {
        return paymentLookupService.sumAmountByStatus(status);
    }

    /**
     * Sums payment amounts by status within a date range.
     *
     * @param status the payment status to filter by
     * @param start  start of the date range (inclusive)
     * @param end    end of the date range (inclusive)
     * @return total sum of payment amounts in the range
     */
    @Override
    public BigDecimal sumAmountByStatusAndDateRange(PaymentStatus status, Instant start, Instant end) {
        return paymentLookupService.sumAmountByStatusAndDateRange(status, start, end);
    }

    /**
     * Finds paid payments within a date range, ordered by paid-at timestamp ascending.
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of paid payments in ascending order
     */
    @Override
    public List<Payment> findPaidBetweenAsc(Instant start, Instant end) {
        return paymentLookupService.findPaidBetweenAsc(start, end);
    }

    /**
     * Finds payments within a date range matching any of the given statuses.
     *
     * @param start    start of the date range (inclusive)
     * @param end      end of the date range (inclusive)
     * @param statuses list of payment statuses to include
     * @return list of matching payments
     */
    @Override
    public List<Payment> findByDateRangeAndStatuses(Instant start, Instant end, List<PaymentStatus> statuses) {
        return paymentLookupService.findByDateRangeAndStatuses(start, end, statuses);
    }

    /**
     * Retrieves the 10 most recently created payments.
     *
     * @return list of the 10 most recent payments
     */
    @Override
    public List<Payment> findRecentTop10() {
        return paymentLookupService.findRecentTop10();
    }

    /**
     * Aggregates daily revenue for PAID payments within a date range.
     *
     * <p>Returns [paidAt, amount] tuples for efficient grouping in service layer,
     * avoiding full entity loads.</p>
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of [paidAt, amount] tuples for daily revenue aggregation
     */
    @Override
    public List<Object[]> aggregateDailyRevenueBetween(Instant start, Instant end) {
        return paymentLookupService.aggregateDailyRevenueBetween(start, end);
    }

    /**
     * Aggregates revenue by payment method for PAID payments within a date range.
     *
     * <p>Returns [paymentMethod, amount] tuples for efficient grouping in service layer,
     * avoiding full entity loads.</p>
     *
     * @param start start of the date range (inclusive)
     * @param end   end of the date range (inclusive)
     * @return list of [paymentMethod, amount] tuples for payment method aggregation
     */
    @Override
    public List<Object[]> aggregateRevenueByPaymentMethodBetween(Instant start, Instant end) {
        return paymentLookupService.aggregateRevenueByPaymentMethodBetween(start, end);
    }
}
