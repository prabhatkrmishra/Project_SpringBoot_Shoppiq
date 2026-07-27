package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.promo.CartItemPreview;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeResponse;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.PromoCode;
import com.pkmprojects.shoppiq.entity.PromoCodeUsage;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.CouponType;
import com.pkmprojects.shoppiq.enums.DiscountType;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.repository.PromoCodeRepository;
import com.pkmprojects.shoppiq.repository.PromoCodeUsageRepository;
import com.pkmprojects.shoppiq.service.PromoCodeService;
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
 * Handles promo code validation, discount calculation and admin management.
 *
 * <h2>Coupon type constraints</h2>
 * <ul>
 *     <li>{@link CouponType#SINGLE}: all cart items must have quantity == 1.</li>
 *     <li>{@link CouponType#BULK}: at least one cart item must have quantity &gt; 1.</li>
 * </ul>
 *
 * <h2>Minimum item quantity</h2>
 * <p>When {@code minItemQuantity} is set, at least one cart item must have
 * quantity &ge; that threshold.</p>
 *
 * <h2>Discount clamping</h2>
 * <p>The computed discount is always clamped to the eligible subtotal so
 * it never pushes the total negative.</p>
 *
 * @author PrabhatKrMishra
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

    @Override
    @Transactional(readOnly = true)
    public PromoCode validateAndCalculate(String code, User user, BigDecimal subtotal,
                                          List<CartItemPreview> items) {

        PromoCode promoCode = promoCodeRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> PromoCodeNotFoundException.forCode(code));

        Instant now = clock.instant();

        if (!Boolean.TRUE.equals(promoCode.getActive())) {
            throw PromoCodeInactiveException.forCode(code);
        }

        if (now.isBefore(promoCode.getValidFrom())) {
            throw PromoCodeNotYetValidException.forCode(code, promoCode.getValidFrom());
        }

        if (now.isAfter(promoCode.getValidUntil())) {
            throw PromoCodeExpiredException.forCode(code, promoCode.getValidUntil());
        }

        if (promoCode.getUsageLimit() != null
                && promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw PromoCodeUsageLimitExceededException.forCode(code, promoCode.getUsageLimit());
        }

        if (promoCode.getUserUsageLimit() != null) {
            long userUsed = promoCodeUsageRepository.countByPromoCodeIdAndUserId(
                    promoCode.getId(), user.getId());
            if (userUsed >= promoCode.getUserUsageLimit()) {
                throw PromoCodeUserUsageLimitExceededException.forCode(code, promoCode.getUserUsageLimit());
            }
        }

        if (promoCode.getMinOrderAmount() != null
                && subtotal.compareTo(promoCode.getMinOrderAmount()) < 0) {
            throw PromoCodeMinOrderAmountException.forCode(code, promoCode.getMinOrderAmount(), subtotal);
        }

        validateCartConstraints(promoCode, items);

        return promoCode;
    }

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

        freshPromoCode.incrementUsedCount();
        promoCodeRepository.save(freshPromoCode);

        PromoCodeUsage usage = PromoCodeUsage.builder()
                .promoCode(freshPromoCode)
                .user(user)
                .order(order)
                .usedAt(clock.instant())
                .build();

        promoCodeUsageRepository.save(usage);
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

    @Override
    public void delete(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> PromoCodeNotFoundException.forId(id));
        promoCodeRepository.delete(promoCode);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromoCodeResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var promoPage = promoCodeRepository.findAll(pageable);
        return PageResponse.of(promoPage, PromoCodeResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCodeResponse findById(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> PromoCodeNotFoundException.forId(id));
        return PromoCodeResponse.from(promoCode);
    }

    @Override
    public PromoCodeResponse toggleActive(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> PromoCodeNotFoundException.forId(id));
        promoCode.setActive(!promoCode.getActive());
        promoCode = promoCodeRepository.save(promoCode);
        return PromoCodeResponse.from(promoCode);
    }

    @Override
    public PromoCode validateForPreview(String code, BigDecimal subtotal,
                                        List<CartItemPreview> items) {
        String normalizedCode = code.toUpperCase().trim();

        PromoCode promoCode = promoCodeRepository.findByCode(normalizedCode)
                .orElseThrow(() -> PromoCodeNotFoundException.forCode(normalizedCode));

        if (!Boolean.TRUE.equals(promoCode.getActive())) {
            throw PromoCodeInactiveException.forCode(normalizedCode);
        }

        Instant now = clock.instant();
        if (promoCode.getValidFrom() != null && now.isBefore(promoCode.getValidFrom())) {
            throw PromoCodeNotYetValidException.forCode(normalizedCode, promoCode.getValidFrom());
        }
        if (promoCode.getValidUntil() != null && now.isAfter(promoCode.getValidUntil())) {
            throw PromoCodeExpiredException.forCode(normalizedCode, promoCode.getValidUntil());
        }

        if (promoCode.getUsageLimit() != null && promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw PromoCodeUsageLimitExceededException.forCode(normalizedCode, promoCode.getUsageLimit());
        }

        if (promoCode.getMinOrderAmount() != null
                && subtotal.compareTo(promoCode.getMinOrderAmount()) < 0) {
            throw PromoCodeMinOrderAmountException.forCode(
                    normalizedCode, promoCode.getMinOrderAmount(), subtotal);
        }

        validateCartConstraints(promoCode, items);

        return promoCode;
    }
}
