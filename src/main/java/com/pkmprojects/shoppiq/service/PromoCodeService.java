package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.promo.PromoCodeRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeResponse;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.PromoCode;
import com.pkmprojects.shoppiq.entity.User;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contract for promo code validation, application and admin management.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public interface PromoCodeService {

    /**
     * Validates a promo code and calculates the discount amount.
     *
     * <p>Performs all validation checks: existence, active status, validity window,
     * global and per-user usage limits, and minimum order amount. Returns the
     * computed discount amount (never exceeding the subtotal).</p>
     *
     * @param code    the promo code string
     * @param user    the user applying the code
     * @param subtotal the order subtotal before discount
     * @return the validated PromoCode entity and computed discount
     */
    PromoCode validateAndCalculate(String code, User user, BigDecimal subtotal);

    /**
     * Calculates the discount amount for a validated promo code.
     *
     * @param promoCode the validated promo code
     * @param subtotal  the order subtotal
     * @return the discount amount
     */
    BigDecimal calculateDiscount(PromoCode promoCode, BigDecimal subtotal);

    /**
     * Records that a promo code was used on an order.
     *
     * <p>Increments the global usage counter and creates a per-user usage record.</p>
     *
     * @param promoCode the promo code that was applied
     * @param user      the user who used it
     * @param order     the order it was applied to
     */
    void recordUsage(PromoCode promoCode, User user, Order order);

    // =========================================================
    // Admin CRUD
    // =========================================================

    /**
     * Creates a new promo code.
     *
     * @param request promo code payload
     * @return the created promo code response
     */
    PromoCodeResponse create(PromoCodeRequest request);

    /**
     * Deletes a promo code by ID.
     *
     * @param id promo code ID
     */
    void delete(Long id);

    /**
     * Returns all promo codes.
     *
     * @return list of promo code responses
     */
    List<PromoCodeResponse> findAll();

    /**
     * Returns a single promo code by ID.
     *
     * @param id promo code ID
     * @return the promo code response
     */
    PromoCodeResponse findById(Long id);

    /**
     * Toggles the active status of a promo code.
     *
     * @param id promo code ID
     * @return the updated promo code response
     */
    PromoCodeResponse toggleActive(Long id);

    /**
     * Validates a promo code and returns the discount amount (for preview purposes).
     *
     * <p>Checks existence, active status, and validity window. Does not check
     * per-user usage limits (use {@link #validateAndCalculate} at checkout).</p>
     *
     * @param code     the promo code string
     * @param subtotal the order subtotal
     * @return the validated PromoCode entity and computed discount
     */
    PromoCode validateForPreview(String code, BigDecimal subtotal);
}
