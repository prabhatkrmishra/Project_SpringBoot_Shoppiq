package com.pkmprojects.shoppiq.service.promo;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.promo.CartItemPreview;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeResponse;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.promo.PromoCode;
import com.pkmprojects.shoppiq.entity.promo.PromoCodeUsage;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.enums.DiscountType;
import com.pkmprojects.shoppiq.exception.general.promo.*;
import com.pkmprojects.shoppiq.repository.promo.PromoCodeRepository;
import com.pkmprojects.shoppiq.repository.promo.PromoCodeUsageRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * {@link PromoCodeService} implementation handling multi-step validation pipeline,
 * atomic usage counting, discount computation with clamping, and admin CRUD.
 *
 * @author prabhatkrmishra
 * @see PromoCodeService
 * @since 1.0.0
 */
@Service
@Transactional
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;
    private final Clock clock;

    public PromoCodeServiceImpl(PromoCodeRepository promoCodeRepository,
                                PromoCodeUsageRepository promoCodeUsageRepository,
                                Clock clock) {
        this.promoCodeRepository = promoCodeRepository;
        this.promoCodeUsageRepository = promoCodeUsageRepository;
        this.clock = clock;
    }

    // =========================================================
    // Validation & Calculation
    // =========================================================

    /**
     * Validates a promo code against the user and cart, returning the entity if valid.
     *
     * <p>Performs sequential validation: existence -> active -> validity window ->
     * global usage limit -> per-user usage limit -> minimum order amount ->
     * cart constraints. Each failure throws a specific exception subclass.</p>
     *
     * @param code     the promo code string
     * @param user     authenticated user
     * @param subtotal order subtotal before discount
     * @param items    cart line item previews
     * @return the validated promo code entity
     * @throws PromoCodeNotFoundException               if the code does not exist
     * @throws PromoCodeInactiveException               if the code is inactive
     * @throws PromoCodeNotYetValidException            if the code is not yet valid
     * @throws PromoCodeExpiredException                if the code has expired
     * @throws PromoCodeUsageLimitExceededException     if the global usage limit is reached
     * @throws PromoCodeUserUsageLimitExceededException if the per-user usage limit is reached
     * @throws PromoCodeMinOrderAmountException         if the subtotal is below the minimum
     * @throws PromoCodeCartConstraintException         if cart constraints are violated
     */
    @Override
    @Transactional(readOnly = true)
    public PromoCode validateAndCalculate(String code, User user, BigDecimal subtotal,
                                          List<CartItemPreview> items) {

        PromoCode promoCode = validateCommon(code, subtotal, items);

        // Per-user usage check (only in validateAndCalculate, not preview)
        if (promoCode.getUserUsageLimit() != null) {
            long userUsed = promoCodeUsageRepository.countByPromoCodeIdAndUserId(
                    promoCode.getId(), user.getId());
            if (userUsed >= promoCode.getUserUsageLimit()) {
                throw PromoCodeUserUsageLimitExceededException.forCode(
                        promoCode.getCode(), promoCode.getUserUsageLimit());
            }
        }

        return promoCode;
    }

    /**
     * Computes the discount amount for a validated promo code against the cart.
     *
     * <p>Handles percentage vs. fixed discounts, applies max discount caps,
     * and clamps the result to the eligible subtotal so it never exceeds
     * the order total.</p>
     *
     * @param promoCode the validated promo code
     * @param subtotal  order subtotal before discount
     * @param items     cart line item previews (used for BULK coupon eligible subtotal)
     * @return the computed discount amount
     */
    @Override
    public BigDecimal calculateDiscount(PromoCode promoCode, BigDecimal subtotal,
                                        List<CartItemPreview> items) {

        BigDecimal eligibleSubtotal = computeEligibleSubtotal(promoCode, items);
        BigDecimal discount;

        if (promoCode.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal pct = promoCode.getDiscountValue()
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discount = eligibleSubtotal.multiply(pct).setScale(2, RoundingMode.HALF_UP);

            if (promoCode.getMaxDiscountAmount() != null
                    && discount.compareTo(promoCode.getMaxDiscountAmount()) > 0) {
                discount = promoCode.getMaxDiscountAmount();
            }
        } else {
            discount = promoCode.getDiscountValue();
        }

        // Clamp: never exceed the eligible subtotal
        if (discount.compareTo(eligibleSubtotal) > 0) {
            discount = eligibleSubtotal;
        }

        // Also clamp to the overall subtotal as a safety net
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return discount;
    }

    /**
     * Records usage of a promo code, atomically incrementing the used count.
     *
     * <p>Uses a conditional JPQL update to safely increment the counter
     * under concurrent access. If the update affects zero rows, the limit
     * was reached between validation and recording.</p>
     *
     * @param promoCode the promo code to record usage for
     * @param user      the user who applied the code
     * @param order     the order the code was applied to
     * @throws PromoCodeUsageLimitExceededException if the usage limit was reached concurrently
     */
    @Override
    public void recordUsage(PromoCode promoCode, User user, Order order) {
        PromoCode freshPromoCode = promoCodeRepository.findById(promoCode.getId())
                .orElseThrow(() -> PromoCodeNotFoundException.forId(promoCode.getId()));

        if (freshPromoCode.getUsageLimit() != null
                && freshPromoCode.getUsedCount() >= freshPromoCode.getUsageLimit()) {
            throw PromoCodeUsageLimitExceededException.forCode(
                    promoCode.getCode(), freshPromoCode.getUsageLimit());
        }

        if (freshPromoCode.getUserUsageLimit() != null) {
            long userUsed = promoCodeUsageRepository.countByPromoCodeIdAndUserId(
                    freshPromoCode.getId(), user.getId());
            if (userUsed >= freshPromoCode.getUserUsageLimit()) {
                throw PromoCodeUserUsageLimitExceededException.forCode(
                        promoCode.getCode(), freshPromoCode.getUserUsageLimit());
            }
        }

        int updated = promoCodeRepository.incrementUsedCountAtomically(freshPromoCode.getId());
        if (updated == 0) {
            throw PromoCodeUsageLimitExceededException.forCode(
                    promoCode.getCode(), freshPromoCode.getUsageLimit() != null
                            ? freshPromoCode.getUsageLimit() : Integer.MAX_VALUE);
        }

        PromoCodeUsage usage = PromoCodeUsage.builder()
                .promoCode(freshPromoCode)
                .user(user)
                .order(order)
                .usedAt(clock.instant())
                .build();

        try {
            promoCodeUsageRepository.save(usage);
        } catch (DataIntegrityViolationException e) {
            // The DB-level unique constraint on (promo_code_id, user_id) caught a
            // concurrent duplicate -- another request inserted a usage record for
            // this user after the count-check above. Treat it as a limit exceeded.
            throw PromoCodeUserUsageLimitExceededException.forCode(
                    promoCode.getCode(), freshPromoCode.getUserUsageLimit() != null
                            ? freshPromoCode.getUserUsageLimit() : 1);
        }
    }

    // =========================================================
    // Cart constraint validation
    // =========================================================

    /**
     * Validates that the cart contents satisfy the promo code's
     * composition constraints.
     *
     * @param promoCode the promo code to check against
     * @param items     the cart line items
     * @throws PromoCodeCartConstraintException when constraints are violated
     */
    private void validateCartConstraints(PromoCode promoCode, List<CartItemPreview> items) {

        // Coupon type check
        if (promoCode.getCouponType() != null) {
            switch (promoCode.getCouponType()) {
                case SINGLE -> {
                    boolean hasMultiUnit = items.stream()
                            .anyMatch(item -> item.quantity() > 1);
                    if (hasMultiUnit) {
                        throw PromoCodeCartConstraintException
                                .singleCodeRequiresSingleUnitCart(promoCode.getCode());
                    }
                }
                case BULK -> {
                    boolean allSingleUnit = items.stream()
                            .allMatch(item -> item.quantity() <= 1);
                    if (allSingleUnit) {
                        throw PromoCodeCartConstraintException
                                .bulkCodeRequiresMultiUnitCart(promoCode.getCode());
                    }
                }
            }
        }

        // Minimum item quantity check
        if (promoCode.getMinItemQuantity() != null && promoCode.getMinItemQuantity() > 0) {
            int maxQuantity = items.stream()
                    .mapToInt(CartItemPreview::quantity)
                    .max()
                    .orElse(0);
            if (maxQuantity < promoCode.getMinItemQuantity()) {
                throw PromoCodeCartConstraintException.minQuantityNotMet(
                        promoCode.getCode(), promoCode.getMinItemQuantity(), maxQuantity);
            }
        }
    }

    // =========================================================
    // Eligible subtotal computation
    // =========================================================

    /**
     * Computes the subtotal of items eligible for the promo code's discount.
     *
     * <p>For {@link CouponType#BULK} codes with a {@code minItemQuantity},
     * only items meeting the quantity threshold are eligible. For other codes,
     * the full subtotal is used.</p>
     *
     * @param promoCode the promo code
     * @param items     the cart line items
     * @return the eligible subtotal
     */
    private BigDecimal computeEligibleSubtotal(PromoCode promoCode, List<CartItemPreview> items) {

        // For BULK codes with minItemQuantity, only count qualifying items
        if (promoCode.getCouponType() == CouponType.BULK
                && promoCode.getMinItemQuantity() != null
                && promoCode.getMinItemQuantity() > 0) {

            return items.stream()
                    .filter(item -> item.quantity() >= promoCode.getMinItemQuantity())
                    .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // For all other codes, the full subtotal is eligible
        return items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // =========================================================
    // Admin CRUD
    // =========================================================

    /**
     * Creates a new promo code with normalized uppercase code and duplicate check.
     *
     * @param request promo code creation payload
     * @return created promo code response
     * @throws DuplicatePromoCodeException if a code with the same name already exists
     */
    @Override
    public PromoCodeResponse create(PromoCodeRequest request) {

        String normalizedCode = request.code().toUpperCase().trim();

        if (promoCodeRepository.existsByCode(normalizedCode)) {
            throw DuplicatePromoCodeException.forCode(normalizedCode);
        }

        PromoCode promoCode = PromoCode.builder()
                .code(normalizedCode)
                .description(request.description())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderAmount(request.minOrderAmount())
                .maxDiscountAmount(request.maxDiscountAmount())
                .couponType(request.couponType())
                .minItemQuantity(request.minItemQuantity())
                .usageLimit(request.usageLimit())
                .usedCount(0)
                .userUsageLimit(request.userUsageLimit())
                .validFrom(request.validFrom())
                .validUntil(request.validUntil())
                .active(request.active() != null ? request.active() : true)
                .build();

        promoCode = promoCodeRepository.save(promoCode);
        return PromoCodeResponse.from(promoCode);
    }

    /**
     * Updates an existing promo code with duplicate-code checking on name change.
     *
     * @param id      promo code ID
     * @param request promo code update payload
     * @return updated promo code response
     * @throws PromoCodeNotFoundException  if the promo code does not exist
     * @throws DuplicatePromoCodeException if the new code conflicts with another
     */
    @Override
    public PromoCodeResponse update(Long id, PromoCodeRequest request) {

        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> PromoCodeNotFoundException.forId(id));

        String normalizedCode = request.code().toUpperCase().trim();

        // Check for duplicate code if the code is being changed
        if (!normalizedCode.equals(promoCode.getCode())
                && promoCodeRepository.existsByCode(normalizedCode)) {
            throw DuplicatePromoCodeException.forCode(normalizedCode);
        }

        promoCode.setCode(normalizedCode);
        promoCode.setDescription(request.description());
        promoCode.setDiscountType(request.discountType());
        promoCode.setDiscountValue(request.discountValue());
        promoCode.setMinOrderAmount(request.minOrderAmount());
        promoCode.setMaxDiscountAmount(request.maxDiscountAmount());
        promoCode.setCouponType(request.couponType());
        promoCode.setMinItemQuantity(request.minItemQuantity());
        promoCode.setUsageLimit(request.usageLimit());
        promoCode.setUserUsageLimit(request.userUsageLimit());
        promoCode.setValidFrom(request.validFrom());
        promoCode.setValidUntil(request.validUntil());
        if (request.active() != null) {
            promoCode.setActive(request.active());
        }

        promoCode = promoCodeRepository.save(promoCode);
        return PromoCodeResponse.from(promoCode);
    }

    /**
     * Deletes a promo code by ID.
     *
     * @param id promo code ID
     * @throws PromoCodeNotFoundException if the promo code does not exist
     */
    @Override
    public void delete(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> PromoCodeNotFoundException.forId(id));
        promoCodeRepository.delete(promoCode);
    }

    /**
     * Retrieves a paginated list of all promo codes, sorted newest first.
     *
     * @param page zero-based page index
     * @param size page size
     * @return paginated promo code responses
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromoCodeResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var promoPage = promoCodeRepository.findAll(pageable);
        return PageResponse.of(promoPage, PromoCodeResponse::from);
    }

    /**
     * Retrieves a single promo code by ID.
     *
     * @param id promo code ID
     * @return promo code response
     * @throws PromoCodeNotFoundException if the promo code does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public PromoCodeResponse findById(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> PromoCodeNotFoundException.forId(id));
        return PromoCodeResponse.from(promoCode);
    }

    /**
     * Toggles the active status of a promo code.
     *
     * @param id promo code ID
     * @return updated promo code response with toggled active flag
     * @throws PromoCodeNotFoundException if the promo code does not exist
     */
    @Override
    public PromoCodeResponse toggleActive(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> PromoCodeNotFoundException.forId(id));
        promoCode.setActive(!promoCode.getActive());
        promoCode = promoCodeRepository.save(promoCode);
        return PromoCodeResponse.from(promoCode);
    }

    /**
     * Validates a promo code for preview purposes (no usage recording).
     *
     * <p>Performs existence, active, validity window, usage limit, minimum order,
     * and cart constraint checks without recording usage.</p>
     *
     * @param code     the promo code string
     * @param subtotal order subtotal before discount
     * @param items    cart line item previews
     * @return the validated promo code entity
     * @throws PromoCodeNotFoundException           if the code does not exist
     * @throws PromoCodeInactiveException           if the code is inactive
     * @throws PromoCodeNotYetValidException        if the code is not yet valid
     * @throws PromoCodeExpiredException            if the code has expired
     * @throws PromoCodeUsageLimitExceededException if the global usage limit is reached
     * @throws PromoCodeMinOrderAmountException     if the subtotal is below the minimum
     * @throws PromoCodeCartConstraintException     if cart constraints are violated
     */
    @Override
    public PromoCode validateForPreview(String code, BigDecimal subtotal,
                                        List<CartItemPreview> items) {
        return validateCommon(code, subtotal, items);
    }

    // =========================================================
    // Shared validation
    // =========================================================

    /**
     * Common validation logic shared between {@link #validateAndCalculate} and
     * {@link #validateForPreview} (BUG-0015). Checks existence, active status,
     * validity window, global usage limit, minimum order, and cart constraints.
     */
    private PromoCode validateCommon(String code, BigDecimal subtotal,
                                     List<CartItemPreview> items) {
        String normalizedCode = code.toUpperCase().trim();

        PromoCode promoCode = promoCodeRepository.findByCode(normalizedCode)
                .orElseThrow(() -> PromoCodeNotFoundException.forCode(normalizedCode));

        if (!Boolean.TRUE.equals(promoCode.getActive())) {
            throw PromoCodeInactiveException.forCode(promoCode.getCode());
        }

        Instant now = clock.instant();
        if (promoCode.getValidFrom() != null && now.isBefore(promoCode.getValidFrom())) {
            throw PromoCodeNotYetValidException.forCode(promoCode.getCode(), promoCode.getValidFrom());
        }
        if (promoCode.getValidUntil() != null && now.isAfter(promoCode.getValidUntil())) {
            throw PromoCodeExpiredException.forCode(promoCode.getCode(), promoCode.getValidUntil());
        }

        if (promoCode.getUsageLimit() != null && promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw PromoCodeUsageLimitExceededException.forCode(promoCode.getCode(), promoCode.getUsageLimit());
        }

        if (promoCode.getMinOrderAmount() != null
                && subtotal.compareTo(promoCode.getMinOrderAmount()) < 0) {
            throw PromoCodeMinOrderAmountException.forCode(
                    promoCode.getCode(), promoCode.getMinOrderAmount(), subtotal);
        }

        validateCartConstraints(promoCode, items);

        return promoCode;
    }
}
