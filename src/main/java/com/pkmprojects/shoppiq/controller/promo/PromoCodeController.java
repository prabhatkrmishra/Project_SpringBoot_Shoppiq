package com.pkmprojects.shoppiq.controller.promo;

import com.pkmprojects.shoppiq.dto.promo.PromoCodeValidateRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeValidateResponse;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.service.promo.PromoCodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Public controller for promo code validation during checkout.
 *
 * <p>Exposes a single unauthenticated endpoint for validating promo codes and
 * computing discount amounts. This endpoint is called from the checkout page
 * when a customer enters a promo code. It validates the code's eligibility
 * (active, not expired, minimum subtotal met) and returns the calculated
 * discount without applying it — the actual application occurs during order
 * placement.</p>
 *
 * <p>This controller acts as the HTTP boundary for promo code validation. It
 * delegates all business logic — code lookup, eligibility validation, and
 * discount calculation — to {@link PromoCodeService}. The controller handles
 * no business logic beyond request assembly.</p>
 *
 * <p>No authentication is required. The endpoint is mounted under
 * /api/promo-codes.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /api/promo-codes/validate  — validate a promo code and preview discount
 * </pre>
 *
 * @author prabhatkrmishra
 * @see PromoCodeService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/promo-codes")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
        this.promoCodeService = promoCodeService;
    }

    /**
     * Validates a promo code and returns the calculated discount.
     *
     * <p>Performs coupon-type and quantity validation based on the provided
     * cart items. Does not apply the discount — only previews the value
     * for display on the checkout page.</p>
     *
     * @param body the validation request containing code, subtotal, and cart items
     * @return 200 OK with discount details (code, amount, type, value, coupon type)
     */
    @PostMapping("/validate")
    public ResponseEntity<PromoCodeValidateResponse> validate(@Valid @RequestBody PromoCodeValidateRequest body) {
        PromoCode promoCode = promoCodeService.validateForPreview(
                body.code(), body.subtotal(), body.cartItems());
        BigDecimal discount = promoCodeService.calculateDiscount(
                promoCode, body.subtotal(), body.cartItems());

        return ResponseEntity.ok(new PromoCodeValidateResponse(
                promoCode.getCode(),
                discount,
                promoCode.getDiscountType(),
                promoCode.getDiscountValue(),
                promoCode.getCouponType()
        ));
    }
}
