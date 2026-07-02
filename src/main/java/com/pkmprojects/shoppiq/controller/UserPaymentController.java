package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.payment.PaymentResponse;
import com.pkmprojects.shoppiq.dto.payment.PaymentStatusResponse;
import com.pkmprojects.shoppiq.dto.payment.VerifyPaymentRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing payment endpoints for authenticated customers and admins.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code GET  /user/payment/get/{id}}     — get payment detail</li>
 *   <li>{@code POST /user/payment/pay/{id}}     — initiate/retry payment</li>
 *   <li>{@code POST /user/payment/verify}       — verify online payment</li>
 *   <li>{@code PUT  /user/payment/cancel/{id}}  — cancel payment</li>
 *   <li>{@code PUT  /user/payment/refund/{id}}  — refund payment (ADMIN only)</li>
 * </ul>
 *
 * <p>
 * All customer operations validate that the payment belongs to the
 * authenticated user. The refund endpoint is restricted to {@code ADMIN}
 * via {@link com.pkmprojects.shoppiq.config.SecurityConfig}.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/user/payment")
@RequiredArgsConstructor
public class UserPaymentController {

    private final PaymentService paymentService;

    // =========================================================
    // Get
    // =========================================================

    /**
     * Returns the full payment detail for the authenticated user.
     *
     * @param user      authenticated customer
     * @param paymentId payment id (must be positive)
     * @return 200 OK with full payment response
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
            @AuthenticationPrincipal User user,
            @PathVariable("id") @Positive(message = "Payment id must be a positive number.") Long paymentId) {

        return ResponseEntity.ok(paymentService.getPayment(user, paymentId));
    }

    // =========================================================
    // Pay
    // =========================================================

    /**
     * Initiates or retries payment for a PENDING or FAILED payment.
     *
     * @param user      authenticated customer
     * @param paymentId payment id (must be positive)
     * @return 200 OK with updated payment status
     */
    @PostMapping("/pay/{id}")
    public ResponseEntity<PaymentStatusResponse> pay(
            @AuthenticationPrincipal User user,
            @PathVariable("id") @Positive(message = "Payment id must be a positive number.") Long paymentId) {

        return ResponseEntity.ok(paymentService.pay(user, paymentId));
    }

    // =========================================================
    // Verify
    // =========================================================

    /**
     * Verifies an online payment using the gateway transaction ID.
     *
     * @param user    authenticated customer
     * @param request contains the transactionId from the gateway
     * @return 200 OK with updated payment status
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentStatusResponse> verifyPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VerifyPaymentRequest request) {

        return ResponseEntity.ok(paymentService.verifyPayment(user, request.transactionId()));
    }

    // =========================================================
    // Cancel
    // =========================================================

    /**
     * Cancels a PENDING or FAILED payment.
     *
     * @param user      authenticated customer
     * @param paymentId payment id (must be positive)
     * @return 200 OK with updated payment status
     */
    @PutMapping("/cancel/{id}")
    public ResponseEntity<PaymentStatusResponse> cancelPayment(
            @AuthenticationPrincipal User user,
            @PathVariable("id") @Positive(message = "Payment id must be a positive number.") Long paymentId) {

        return ResponseEntity.ok(paymentService.cancelPayment(user, paymentId));
    }
}
