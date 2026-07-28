package com.pkmprojects.shoppiq.service.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <strong>Spring Boot Concept:</strong> Default implementation of {@link PaymentWriteService}.
 *
 * <p>Thin write facade that delegates {@code save()} calls to
 * {@code PaymentRepository}. Separated from {@link PaymentLookupService}
 * (read) to follow the single-responsibility principle and CQRS-like
 * separation at the service layer.</p>
 *
 * <p><strong>Educational value:</strong></p>
 * <ul>
 *   <li><strong>@Service</strong> — Registers this package-private class as a Spring bean.</li>
 *   <li><strong>@RequiredArgsConstructor</strong> — Lombok generates the constructor for
 *       {@code PaymentRepository}, used by Spring for constructor injection.</li>
 *   <li><strong>@Transactional</strong> — Each {@code save()} call runs in its own transaction.
 *       When called from {@link PaymentServiceImpl}, the transaction propagates to the outer
 *       one via Spring's default {@code REQUIRED} propagation.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class PaymentWriteServiceImpl implements PaymentWriteService {

    private final PaymentRepository paymentRepository;

    /**
     * Persists the given payment entity.
     *
     * <p>When called from {@link PaymentServiceImpl}, the transaction
     * propagates to the outer one via Spring's REQUIRED propagation.</p>
     *
     * @param payment the payment entity to save
     * @return the saved payment entity
     */
    @Override
    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
}
