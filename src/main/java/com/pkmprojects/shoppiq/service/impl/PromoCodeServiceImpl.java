package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeResponse;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.PromoCode;
import com.pkmprojects.shoppiq.entity.PromoCodeUsage;
import com.pkmprojects.shoppiq.entity.User;
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
import java.time.Instant;

/**
 * Handles promo code validation, discount calculation and admin management.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Service
@Transactional
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;

    public PromoCodeServiceImpl(PromoCodeRepository promoCodeRepository,
                                PromoCodeUsageRepository promoCodeUsageRepository) {
        this.promoCodeRepository = promoCodeRepository;
        this.promoCodeUsageRepository = promoCodeUsageRepository;
    }

    // =========================================================
    // Validation & Calculation
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PromoCode validateAndCalculate(String code, User user, BigDecimal subtotal) {

        PromoCode promoCode = promoCodeRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> PromoCodeNotFoundException.forCode(code));

        Instant now = Instant.now();

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

        return promoCode;
    }

    @Override
    public BigDecimal calculateDiscount(PromoCode promoCode, BigDecimal subtotal) {

        BigDecimal discount;

        if (promoCode.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal pct = promoCode.getDiscountValue()
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discount = subtotal.multiply(pct).setScale(2, RoundingMode.HALF_UP);

            if (promoCode.getMaxDiscountAmount() != null
                    && discount.compareTo(promoCode.getMaxDiscountAmount()) > 0) {
                discount = promoCode.getMaxDiscountAmount();
            }
        } else {
            discount = promoCode.getDiscountValue();
        }

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
                .usedAt(Instant.now())
                .build();

        promoCodeUsageRepository.save(usage);
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
    public PromoCode validateForPreview(String code, BigDecimal subtotal) {
        String normalizedCode = code.toUpperCase().trim();

        PromoCode promoCode = promoCodeRepository.findByCode(normalizedCode)
                .orElseThrow(() -> PromoCodeNotFoundException.forCode(normalizedCode));

        if (!Boolean.TRUE.equals(promoCode.getActive())) {
            throw PromoCodeInactiveException.forCode(normalizedCode);
        }

        Instant now = Instant.now();
        if (promoCode.getValidFrom() != null && now.isBefore(promoCode.getValidFrom())) {
            throw PromoCodeNotYetValidException.forCode(normalizedCode, promoCode.getValidFrom());
        }
        if (promoCode.getValidUntil() != null && now.isAfter(promoCode.getValidUntil())) {
            throw PromoCodeExpiredException.forCode(normalizedCode, promoCode.getValidUntil());
        }

        if (promoCode.getUsageLimit() != null && promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw PromoCodeUsageLimitExceededException.forCode(normalizedCode, promoCode.getUsageLimit());
        }

        return promoCode;
    }
}
