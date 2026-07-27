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
 * Default implementation of {@link AdminPaymentReadModel}.
 *
 * <p>Delegates to {@link com.pkmprojects.shoppiq.service.payment.PaymentLookupService}
 * for payment aggregate queries used in admin dashboards and reports.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AdminPaymentReadModelImpl implements AdminPaymentReadModel {

    private final PaymentLookupService paymentLookupService;

    @Override
    public BigDecimal sumAmountByStatus(PaymentStatus status) {
        return paymentLookupService.sumAmountByStatus(status);
    }

    @Override
    public BigDecimal sumAmountByStatusAndDateRange(PaymentStatus status, Instant start, Instant end) {
        return paymentLookupService.sumAmountByStatusAndDateRange(status, start, end);
    }

    @Override
    public List<Payment> findPaidBetweenAsc(Instant start, Instant end) {
        return paymentLookupService.findPaidBetweenAsc(start, end);
    }

    @Override
    public List<Payment> findByDateRangeAndStatuses(Instant start, Instant end, List<PaymentStatus> statuses) {
        return paymentLookupService.findByDateRangeAndStatuses(start, end, statuses);
    }

    @Override
    public List<Payment> findRecentTop10() {
        return paymentLookupService.findRecentTop10();
    }
}
