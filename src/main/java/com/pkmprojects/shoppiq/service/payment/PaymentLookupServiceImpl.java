package com.pkmprojects.shoppiq.service.payment;

import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link PaymentLookupService} implementation providing read-only payment queries.
 *
 * @author prabhatkrmishra
 * @see PaymentLookupService
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    /**
     * Safely casts a JPQL projection value to the expected type.
     *
     * @param value the value from the Object[] tuple
     * @param type  the expected type
     * @param <T>   the cast target type
     * @return the cast value
     * @throws ClassCastException if the value is not of the expected type
     */
    @SuppressWarnings("unchecked")
    private static <T> T safeCast(Object value, Class<T> type) {
        return type.cast(value);
    }

    /**
     * Finds a payment by ID.
     *
     * @param paymentId the payment ID
     * @return optional containing the payment if found
     */
    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    /**
     * Checks whether a payment record exists for the given order.
     *
     * @param order the order entity
     * @return true if a payment exists for the order
     */
    @Override
    public boolean existsByOrder(Order order) {
        return paymentRepository.existsByOrder(order);
    }

    /**
     * Returns the total number of payment records.
     *
     * @return total payment count
     */
    @Override
    public long count() {
        return paymentRepository.count();
    }

    /**
     * Counts payments by their current status.
     *
     * @param status the payment status to count
     * @return count of payments with the given status
     */
    @Override
    public long countByStatus(PaymentStatus status) {
        return paymentRepository.countByPaymentStatus(status);
    }

    /**
     * Sums the total amount of payments with the given status.
     *
     * @param status the payment status to filter by
     * @return total sum of payment amounts
     */
    @Override
    public BigDecimal sumAmountByStatus(PaymentStatus status) {
        return paymentRepository.sumAmountByPaymentStatus(status);
    }

    /**
     * Sums the total amount of payments for a specific user with the given status.
     *
     * @param user   the user entity
     * @param status the payment status to filter by
     * @return total sum of payment amounts for the user
     */
    @Override
    public BigDecimal sumAmountByUserAndStatus(User user, PaymentStatus status) {
        return paymentRepository.sumAmountByUserAndStatus(user, status);
    }

    /**
     * Finds a paginated list of payments filtered by status.
     *
     * @param status the payment status to filter by
     * @param page   zero-based page index
     * @param size   page size
     * @return paginated payment results
     */
    @Override
    public Page<Payment> findByStatus(PaymentStatus status, int page, int size) {
        return paymentRepository.findByPaymentStatus(status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    /**
     * Retrieves a paginated list of all payments.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated payment results
     */
    @Override
    public Page<Payment> findAll(int page, int size) {
        return paymentRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
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
        return paymentRepository.sumAmountByStatusAndDateRange(status, start, end);
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
        return paymentRepository.findByPaymentStatusAndPaidAtBetweenOrderByPaidAtAsc(
                PaymentStatus.PAID, start, end);
    }

    /**
     * Finds payments created within a date range matching any of the given statuses.
     *
     * @param start    start of the date range (inclusive)
     * @param end      end of the date range (inclusive)
     * @param statuses list of payment statuses to include
     * @return list of matching payments
     */
    @Override
    public List<Payment> findByDateRangeAndStatuses(Instant start, Instant end, List<PaymentStatus> statuses) {
        return paymentRepository.findByCreatedAtBetweenAndPaymentStatusIn(start, end, statuses);
    }

    /**
     * Retrieves the 10 most recently created payments.
     *
     * @return list of the 10 most recent payments
     */
    @Override
    public List<Payment> findRecentTop10() {
        return paymentRepository.findTop10ByOrderByCreatedAtDesc();
    }

    /**
     * Batch-sums paid amounts for the given user IDs — avoids N+1
     * when enriching a list of users with their total spent (BUG-003).
     *
     * @param userIds the user IDs to sum paid amounts for
     * @return map of userId → total paid amount (0 for users with no payments)
     */
    @Override
    public Map<Long, BigDecimal> sumPaidAmountByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> results = paymentRepository.sumAmountByUserIdsAndStatus(
                userIds, PaymentStatus.PAID);
        Map<Long, BigDecimal> resultMap = new HashMap<>(userIds.size());
        for (Object[] row : results) {
            resultMap.put(safeCast(row[0], Long.class), safeCast(row[1], BigDecimal.class));
        }
        return resultMap;
    }

    /**
     * Aggregates daily revenue from PAID payments within a date range.
     */
    @Override
    public List<Object[]> aggregateDailyRevenueBetween(Instant start, Instant end) {
        return paymentRepository.aggregateDailyRevenueBetween(start, end);
    }

    /**
     * Aggregates revenue by payment method for PAID payments within a date range.
     */
    @Override
    public List<Object[]> aggregateRevenueByPaymentMethodBetween(Instant start, Instant end) {
        return paymentRepository.aggregateRevenueByPaymentMethodBetween(start, end);
    }
}
