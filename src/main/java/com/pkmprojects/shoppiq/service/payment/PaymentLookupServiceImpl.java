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
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link PaymentLookupService}.
 *
 * <p>All queries run in a read-only transaction. Delegates directly
 * to {@code PaymentRepository} with appropriate sort ordering.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    @Override
    public Optional<Payment> findById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public boolean existsByOrder(Order order) {
        return paymentRepository.existsByOrder(order);
    }

    @Override
    public long count() {
        return paymentRepository.count();
    }

    @Override
    public long countByStatus(PaymentStatus status) {
        return paymentRepository.countByPaymentStatus(status);
    }

    @Override
    public BigDecimal sumAmountByStatus(PaymentStatus status) {
        return paymentRepository.sumAmountByPaymentStatus(status);
    }

    @Override
    public BigDecimal sumAmountByUserAndStatus(User user, PaymentStatus status) {
        return paymentRepository.sumAmountByUserAndStatus(user, status);
    }

    @Override
    public Page<Payment> findByStatus(PaymentStatus status, int page, int size) {
        return paymentRepository.findByPaymentStatus(status,
                PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @Override
    public Page<Payment> findAll(int page, int size) {
        return paymentRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @Override
    public BigDecimal sumAmountByStatusAndDateRange(PaymentStatus status, Instant start, Instant end) {
        return paymentRepository.sumAmountByStatusAndDateRange(status, start, end);
    }

    @Override
    public List<Payment> findPaidBetweenAsc(Instant start, Instant end) {
        return paymentRepository.findByPaymentStatusAndPaidAtBetweenOrderByPaidAtAsc(
                PaymentStatus.PAID, start, end);
    }

    @Override
    public List<Payment> findByDateRangeAndStatuses(Instant start, Instant end, List<PaymentStatus> statuses) {
        return paymentRepository.findByCreatedAtBetweenAndPaymentStatusIn(start, end, statuses);
    }

    @Override
    public List<Payment> findRecentTop10() {
        return paymentRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
