package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.entity.PromoCode;
import com.pkmprojects.shoppiq.service.PromoCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

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
     * @return discount amount
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        BigDecimal subtotal = new BigDecimal(body.get("subtotal").toString());

        PromoCode promoCode = promoCodeService.validateForPreview(code, subtotal);
        BigDecimal discount = promoCodeService.calculateDiscount(promoCode, subtotal);

        return ResponseEntity.ok(Map.of(
                "code", promoCode.getCode(),
                "discount", discount,
                "discountType", promoCode.getDiscountType(),
                "discountValue", promoCode.getDiscountValue()
        ));
    }
}
