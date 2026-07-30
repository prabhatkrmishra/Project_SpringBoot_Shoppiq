package com.pkmprojects.shoppiq.service.promo;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.promo.CartItemPreview;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeResponse;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.entity.user.User;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business contract for promo code validation, application, and admin management.
 *
 * <p>Defines operations for multi-step validation pipeline, discount calculation,
 * usage recording, and standard admin CRUD with toggle-active.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public interface PromoCodeService {

    /**
     * Validates a promo code and calculates the discount amount.
     *
     * <p>Performs all validation checks: existence, active status, validity window,
     * global and per-user usage limits, minimum order amount, and cart composition
     * constraints (coupon type, minimum item quantity). Returns the computed
     * discount amount (never exceeding the eligible subtotal).</p>
     *
     * @param code     the promo code string
     * @param user     the user applying the code
     * @param subtotal the order subtotal before discount
     * @param items    the cart line items for coupon-type and quantity validation
     * @return the validated PromoCode entity and computed discount
     */
    PromoCode validateAndCalculate(String code, User user, BigDecimal subtotal, List<CartItemPreview> items);

    /**
     * Calculates the discount amount for a validated promo code.
     *
     * <p>For {@link com.pkmprojects.shoppiq.enums.CouponType#BULK} codes,
     * the discount is computed against the eligible subtotal (items whose
     * quantity meets the minimum threshold). For other codes, the full
     * subtotal is used.</p>
     *
     * @param promoCode the validated promo code
     * @param subtotal  the order subtotal
     * @param items     the cart line items for eligible-subtotal calculation
     * @return the discount amount (clamped to the eligible subtotal)
     */
    BigDecimal calculateDiscount(PromoCode promoCode, BigDecimal subtotal, List<CartItemPreview> items);

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
     * Returns all promo codes, paginated.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return paginated promo code responses
     */
    PageResponse<PromoCodeResponse> findAll(int page, int size);

    /**
     * Returns a single promo code by ID.
     *
     * @param id promo code ID
     * @return the promo code response
     */
    PromoCodeResponse findById(Long id);

    /**
     * Updates an existing promo code.
     *
     * @param id      promo code ID
     * @param request updated promo code payload
     * @return the updated promo code response
     */
    PromoCodeResponse update(Long id, PromoCodeRequest request);

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
     * <p>Checks existence, active status, validity window, global usage limit,
     * minimum order amount, and cart composition constraints. Does not check
     * per-user usage limits (use {@link #validateAndCalculate} at checkout).</p>
     *
     * @param code     the promo code string
     * @param subtotal the order subtotal
     * @param items    the cart line items for coupon-type and quantity validation
     * @return the validated PromoCode entity and computed discount
     */
    PromoCode validateForPreview(String code, BigDecimal subtotal, List<CartItemPreview> items);
}
