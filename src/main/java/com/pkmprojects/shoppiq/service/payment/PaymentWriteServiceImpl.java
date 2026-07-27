package com.pkmprojects.shoppiq.service.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;
import com.pkmprojects.shoppiq.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link PaymentWriteService}.
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class PaymentWriteServiceImpl implements PaymentWriteService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
}
