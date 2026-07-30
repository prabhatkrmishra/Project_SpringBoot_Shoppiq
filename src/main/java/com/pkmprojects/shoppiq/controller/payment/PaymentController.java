package com.pkmprojects.shoppiq.controller.payment;

import com.pkmprojects.shoppiq.dto.payment.PaymentResponse;
import com.pkmprojects.shoppiq.dto.payment.PaymentStatusResponse;
import com.pkmprojects.shoppiq.dto.payment.VerifyPaymentRequest;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.service.payment.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing payment endpoints for authenticated customers and admins.
 *
 * <p>Provides endpoints for retrieving payment details, initiating or retrying
 * payments, verifying online transactions, cancelling pending payments, and
 * processing refunds. All customer operations validate that the payment belongs
 * to the authenticated user. The refund endpoint is restricted to ADMIN via
 * method-level security.</p>
 *
 * <p>This controller acts as the HTTP boundary for payment operations. It
 * delegates all business logic — payment retrieval, gateway integration,
 * transaction verification, cancellation, and refund processing — to
 * {@link PaymentService}. The controller handles no business logic beyond
 * request validation and authentication extraction.</p>
 *
 * <p>All endpoints are scoped to /user/payment. Customer endpoints require
 * authentication; the refund endpoint additionally requires ADMIN role.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * GET    /user/payment/get/{id}     — get payment detail
 * POST   /user/payment/pay/{id}     — initiate or retry payment
 * POST   /user/payment/verify       — verify online payment via gateway
 * PUT    /user/payment/cancel/{id}  — cancel a pending or failed payment
 * PUT    /user/payment/refund/{id}  — refund a completed payment (ADMIN only)
 * </pre>
 *
 * @author prabhatkrmishra
 * @see PaymentService
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/user/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // =========================================================
    // Get
    // =========================================================

    /**
     * Returns the full payment detail for the authenticated user.
     *
     * @param user      the authenticated customer
     * @param paymentId the payment ID to retrieve (must be positive)
     * @return 200 OK with full payment response
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Payment id must be a positive number.") Long paymentId) {

        return ResponseEntity.ok(paymentService.getPayment(user, paymentId));
    }

    // =========================================================
    // Pay
    // =========================================================

    /**
     * Initiates or retries payment for a PENDING or FAILED payment.
     *
     * <p>Generates a new payment session with the gateway and returns
     * the updated payment status.</p>
     *
     * @param user      the authenticated customer
     * @param paymentId the payment ID to pay (must be positive)
     * @return 200 OK with updated payment status response
     */
    @PostMapping("/pay/{id}")
    public ResponseEntity<PaymentStatusResponse> pay(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Payment id must be a positive number.") Long paymentId) {

        return ResponseEntity.ok(paymentService.pay(user, paymentId));
    }

    // =========================================================
    // Verify
    // =========================================================

    /**
     * Verifies an online payment using the gateway transaction ID.
     *
     * <p>Calls the payment gateway to confirm the transaction status
     * and updates the local payment record accordingly.</p>
     *
     * @param user    the authenticated customer
     * @param request the verification payload containing paymentId and transactionId
     * @return 200 OK with updated payment status response
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentStatusResponse> verifyPayment(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody VerifyPaymentRequest request) {

        return ResponseEntity.ok(paymentService.verifyPayment(user, request.paymentId(), request.transactionId()));
    }

    // =========================================================
    // Cancel
    // =========================================================

    /**
     * Cancels a PENDING or FAILED payment.
     *
     * <p>Only payments in PENDING or FAILED status can be cancelled.
     * Once a payment is PAID, it cannot be cancelled (use refund instead).</p>
     *
     * @param user      the authenticated customer
     * @param paymentId the payment ID to cancel (must be positive)
     * @return 200 OK with updated payment status response
     */
    @PutMapping("/cancel/{id}")
    public ResponseEntity<PaymentStatusResponse> cancelPayment(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Payment id must be a positive number.") Long paymentId) {

        return ResponseEntity.ok(paymentService.cancelPayment(user, paymentId));
    }

    // =========================================================
    // Refund (ADMIN only)
    // =========================================================

    /**
     * Refunds a completed (PAID) payment. Restricted to ADMIN via method
     * security; ownership is not enforced since admins may refund any payment.
     *
     * @param user      the authenticated admin
     * @param paymentId the payment ID to refund (must be positive)
     * @return 200 OK with updated payment status response
     */
    @PutMapping("/refund/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentStatusResponse> refundPayment(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("id") @Positive(message = "Payment id must be a positive number.") Long paymentId) {

        return ResponseEntity.ok(paymentService.refund(user, paymentId));
    }
}
