package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.promo.PromoCodeValidateRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeValidateResponse;
import com.pkmprojects.shoppiq.entity.PromoCode;
import com.pkmprojects.shoppiq.service.PromoCodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Public controller for promo code validation.
 *
 * @author PrabhatKrMishra
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
     * Validates a promo code and returns the discount amount.
     *
     * @param body request body containing code and subtotal
     * @return discount details
     */
    @PostMapping("/validate")
    public ResponseEntity<PromoCodeValidateResponse> validate(@Valid @RequestBody PromoCodeValidateRequest body) {
        PromoCode promoCode = promoCodeService.validateForPreview(body.code(), body.subtotal());
        BigDecimal discount = promoCodeService.calculateDiscount(promoCode, body.subtotal());

        return ResponseEntity.ok(new PromoCodeValidateResponse(
                promoCode.getCode(),
                discount,
                promoCode.getDiscountType(),
                promoCode.getDiscountValue()
        ));
    }
}
