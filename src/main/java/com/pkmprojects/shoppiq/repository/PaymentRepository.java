package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link Payment} persistence.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds the payment record associated with the given order.
     *
     * @param order the parent order
     * @return optional payment
     */
    Optional<Payment> findByOrder(Order order);

    /**
     * Finds a payment by its internal human-readable reference.
     *
     * @param paymentReference e.g. {@code PAY-20260701-42}
     * @return optional payment
     */
    Optional<Payment> findByPaymentReference(String paymentReference);

    /**
     * Finds a payment by the external transaction ID returned by the gateway.
     *
     * @param transactionId gateway transaction ID
     * @return optional payment
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Returns {@code true} if a payment already exists for the given order.
     *
     * @param order the parent order
     * @return {@code true} if a payment record exists
     */
    boolean existsByOrder(Order order);
}
