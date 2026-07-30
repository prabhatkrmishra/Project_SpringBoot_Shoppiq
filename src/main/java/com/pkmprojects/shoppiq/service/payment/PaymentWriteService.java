package com.pkmprojects.shoppiq.service.payment;

import com.pkmprojects.shoppiq.entity.payment.Payment;

/**
 * Write facade for payment persistence.
 *
 * <p>Decouples caller code from {@code PaymentRepository},
 * providing save operations for payment lifecycle management.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface PaymentWriteService {

    /**
     * Persists a new or updated payment.
     */
    Payment save(Payment payment);
}
