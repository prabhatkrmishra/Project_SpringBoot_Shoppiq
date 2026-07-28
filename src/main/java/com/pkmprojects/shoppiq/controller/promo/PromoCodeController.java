package com.pkmprojects.shoppiq.controller.promo;

import com.pkmprojects.shoppiq.dto.promo.PromoCodeValidateRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeValidateResponse;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.service.promo.PromoCodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * <strong>Spring Boot Concept:</strong> Public controller for promo code validation.
 *
 * <p>Exposes a single unauthenticated endpoint for validating promo codes
 * and computing discount amounts during checkout. Delegates all business
 * logic to {@link PromoCodeService}.</p>
 *
 * <p>Key design points:
 * <ul>
 *   <li><strong>Thin controller</strong> — no business logic; validates input and delegates to service layer.</li>
 *   <li><strong>Public endpoint</strong> — no authentication required so the checkout page can validate codes before login.</li>
 * </ul>
 * </p>
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
     * cart items. Does not apply the discount — only previews the value.</p>
     *
     * @param body request body containing the promo code, subtotal, and cart items
     * @return 200 OK with discount details (code, discount amount, discount type, value, coupon type)
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
