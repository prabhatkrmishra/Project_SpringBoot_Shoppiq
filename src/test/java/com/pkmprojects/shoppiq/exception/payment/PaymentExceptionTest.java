package com.pkmprojects.shoppiq.exception.payment;

import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for payment-related exception classes.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DisplayName("Payment Exception Tests")
class PaymentExceptionTest {

    @Test
    @DisplayName("PaymentNotFoundException.forId — 404 with PAYMENT_NOT_FOUND")
    void paymentNotFound_forId() {
        PaymentNotFoundException ex = PaymentNotFoundException.forId(42L);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getDetail()).contains("42");
    }

    @Test
    @DisplayName("PaymentNotFoundException.forTransactionId — contains txnId in detail")
    void paymentNotFound_forTransactionId() {
        PaymentNotFoundException ex = PaymentNotFoundException.forTransactionId("TXN-999");

        assertThat(ex.getDetail()).contains("TXN-999");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PaymentNotFoundException.forOrder — contains orderId in detail")
    void paymentNotFound_forOrder() {
        PaymentNotFoundException ex = PaymentNotFoundException.forOrder(5L);

        assertThat(ex.getDetail()).contains("5");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PaymentAccessDeniedException.forPayment — 403 with PAYMENT_ACCESS_DENIED")
    void paymentAccessDenied_forPayment() {
        PaymentAccessDeniedException ex = PaymentAccessDeniedException.forPayment(7L);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_ACCESS_DENIED);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getDetail()).contains("7");
    }

    @Test
    @DisplayName("DuplicatePaymentException — 409 with PAYMENT_ALREADY_EXISTS")
    void duplicatePayment() {
        DuplicatePaymentException ex = new DuplicatePaymentException(10L);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_ALREADY_EXISTS);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getDetail()).contains("10");
    }

    @Test
    @DisplayName("PaymentInvalidStateException.alreadyPaid — 400 with PAYMENT_INVALID_STATE")
    void paymentInvalidState_alreadyPaid() {
        PaymentInvalidStateException ex = PaymentInvalidStateException.alreadyPaid(3L);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_INVALID_STATE);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getDetail()).contains("3");
    }

    @Test
    @DisplayName("PaymentInvalidStateException.cannotCancel — contains status in detail")
    void paymentInvalidState_cannotCancel() {
        PaymentInvalidStateException ex =
                PaymentInvalidStateException.cannotCancel(2L, PaymentStatus.PAID);

        assertThat(ex.getDetail()).contains("PAID");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("PaymentInvalidStateException.refundNotAllowed — contains PENDING in detail")
    void paymentInvalidState_refundNotAllowed() {
        PaymentInvalidStateException ex =
                PaymentInvalidStateException.refundNotAllowed(5L, PaymentStatus.PENDING);

        assertThat(ex.getDetail()).contains("PENDING");
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("PaymentInvalidStateException.cannotPay — contains current status in detail")
    void paymentInvalidState_cannotPay() {
        PaymentInvalidStateException ex =
                PaymentInvalidStateException.cannotPay(1L, PaymentStatus.CANCELLED);

        assertThat(ex.getDetail()).contains("CANCELLED");
    }
}