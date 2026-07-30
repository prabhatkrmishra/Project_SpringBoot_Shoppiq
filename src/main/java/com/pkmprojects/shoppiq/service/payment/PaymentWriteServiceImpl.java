package com.pkmprojects.shoppiq.service.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link PaymentWriteService} implementation providing transactional persistence
 * for payment entities.
 *
 * @author prabhatkrmishra
 * @see PaymentWriteService
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
