package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.promo.PromoCodeRequest;
import com.pkmprojects.shoppiq.dto.promo.PromoCodeResponse;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.PromoCode;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.DiscountType;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.repository.PromoCodeRepository;
import com.pkmprojects.shoppiq.repository.PromoCodeUsageRepository;
import com.pkmprojects.shoppiq.service.impl.PromoCodeServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PromoCodeServiceImpl}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PromoCodeServiceImpl Tests")
class PromoCodeServiceImplTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;
    @Mock
    private PromoCodeUsageRepository promoCodeUsageRepository;

    @InjectMocks
    private PromoCodeServiceImpl promoCodeService;

    // ─── Helpers ──────────────────────────────────────────────────────────

    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private User buildUser(long id) throws Exception {
        User user = User.builder()
                .name("Alice").username("alice")
                .email("alice@test.com").password("hashed")
                .enabled(true).build();
        setId(user, id);
        return user;
    }

    private PromoCode buildPromoCode(Long id, String code, DiscountType type,
                                      BigDecimal value, BigDecimal minAmount,
                                      BigDecimal maxDiscount, Integer usageLimit,
                                      Integer userUsageLimit, int usedCount,
                                      Instant validFrom, Instant validUntil,
                                      boolean active) throws Exception {
        PromoCode pc = PromoCode.builder()
                .code(code)
                .description("Test promo")
                .discountType(type)
                .discountValue(value)
                .minOrderAmount(minAmount)
                .maxDiscountAmount(maxDiscount)
                .usageLimit(usageLimit)
                .usedCount(usedCount)
                .userUsageLimit(userUsageLimit)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .active(active)
                .build();
        setId(pc, id);
        return pc;
    }

    private PromoCodeRequest buildRequest(String code, DiscountType type,
                                           BigDecimal value, BigDecimal minAmount,
                                           BigDecimal maxDiscount, Integer usageLimit,
                                           Integer userUsageLimit,
                                           Instant validFrom, Instant validUntil) {
        return new PromoCodeRequest(
                code, "Test promo", type, value,
                minAmount, maxDiscount, usageLimit, userUsageLimit,
                validFrom, validUntil, true
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // validateAndCalculate()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validateAndCalculate()")
    class ValidateAndCalculateTests {

        @Test
        @DisplayName("Success — valid percentage promo code")
        void validateAndCalculate_success_percentage() throws Exception {
            User user = buildUser(1L);
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(10L, "SAVE20", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(20), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            when(promoCodeRepository.findByCode("SAVE20")).thenReturn(Optional.of(pc));

            PromoCode result = promoCodeService.validateAndCalculate("SAVE20", user, BigDecimal.valueOf(500));

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("SAVE20");
        }

        @Test
        @DisplayName("Success — valid fixed amount promo code")
        void validateAndCalculate_success_fixedAmount() throws Exception {
            User user = buildUser(1L);
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(10L, "FLAT50", DiscountType.FIXED_AMOUNT,
                    BigDecimal.valueOf(50), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            when(promoCodeRepository.findByCode("FLAT50")).thenReturn(Optional.of(pc));

            PromoCode result = promoCodeService.validateAndCalculate("FLAT50", user, BigDecimal.valueOf(500));

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("FLAT50");
        }

        @Test
        @DisplayName("Fails — promo code not found")
        void validateAndCalculate_notFound() throws Exception {
            User user = buildUser(1L);
            when(promoCodeRepository.findByCode("NOPE")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    promoCodeService.validateAndCalculate("NOPE", user, BigDecimal.valueOf(500))
            ).isInstanceOf(PromoCodeNotFoundException.class);
        }

        @Test
        @DisplayName("Fails — promo code inactive")
        void validateAndCalculate_inactive() throws Exception {
            User user = buildUser(1L);
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(10L, "INACTIVE", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), false);

            when(promoCodeRepository.findByCode("INACTIVE")).thenReturn(Optional.of(pc));

            assertThatThrownBy(() ->
                    promoCodeService.validateAndCalculate("INACTIVE", user, BigDecimal.valueOf(500))
            ).isInstanceOf(PromoCodeInactiveException.class);
        }

        @Test
        @DisplayName("Fails — promo code not yet valid")
        void validateAndCalculate_notYetValid() throws Exception {
            User user = buildUser(1L);
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(10L, "FUTURE", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, null, null, 0,
                    now.plus(7, ChronoUnit.DAYS), now.plus(37, ChronoUnit.DAYS), true);

            when(promoCodeRepository.findByCode("FUTURE")).thenReturn(Optional.of(pc));

            assertThatThrownBy(() ->
                    promoCodeService.validateAndCalculate("FUTURE", user, BigDecimal.valueOf(500))
            ).isInstanceOf(PromoCodeNotYetValidException.class);
        }

        @Test
        @DisplayName("Fails — promo code expired")
        void validateAndCalculate_expired() throws Exception {
            User user = buildUser(1L);
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(10L, "EXPIRED", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, null, null, 0,
                    now.minus(30, ChronoUnit.DAYS), now.minus(1, ChronoUnit.DAYS), true);

            when(promoCodeRepository.findByCode("EXPIRED")).thenReturn(Optional.of(pc));

            assertThatThrownBy(() ->
                    promoCodeService.validateAndCalculate("EXPIRED", user, BigDecimal.valueOf(500))
            ).isInstanceOf(PromoCodeExpiredException.class);
        }

        @Test
        @DisplayName("Fails — global usage limit exceeded")
        void validateAndCalculate_usageLimitExceeded() throws Exception {
            User user = buildUser(1L);
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(10L, "LIMITED", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, 5, null, 5,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            when(promoCodeRepository.findByCode("LIMITED")).thenReturn(Optional.of(pc));

            assertThatThrownBy(() ->
                    promoCodeService.validateAndCalculate("LIMITED", user, BigDecimal.valueOf(500))
            ).isInstanceOf(PromoCodeUsageLimitExceededException.class);
        }

        @Test
        @DisplayName("Fails — per-user usage limit exceeded")
        void validateAndCalculate_userUsageLimitExceeded() throws Exception {
            User user = buildUser(1L);
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(10L, "ONCE", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, null, 1, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            when(promoCodeRepository.findByCode("ONCE")).thenReturn(Optional.of(pc));
            when(promoCodeUsageRepository.countByPromoCodeIdAndUserId(10L, 1L)).thenReturn(1L);

            assertThatThrownBy(() ->
                    promoCodeService.validateAndCalculate("ONCE", user, BigDecimal.valueOf(500))
            ).isInstanceOf(PromoCodeUserUsageLimitExceededException.class);
        }

        @Test
        @DisplayName("Fails — minimum order amount not met")
        void validateAndCalculate_minAmountNotMet() throws Exception {
            User user = buildUser(1L);
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(10L, "MIN100", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), BigDecimal.valueOf(100), null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            when(promoCodeRepository.findByCode("MIN100")).thenReturn(Optional.of(pc));

            assertThatThrownBy(() ->
                    promoCodeService.validateAndCalculate("MIN100", user, BigDecimal.valueOf(50))
            ).isInstanceOf(PromoCodeMinOrderAmountException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // calculateDiscount()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("calculateDiscount()")
    class CalculateDiscountTests {

        @Test
        @DisplayName("Percentage discount — no cap")
        void calculateDiscount_percentage() throws Exception {
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(1L, "SAVE20", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(20), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            BigDecimal discount = promoCodeService.calculateDiscount(pc, BigDecimal.valueOf(500));

            assertThat(discount).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("Percentage discount — capped by maxDiscountAmount")
        void calculateDiscount_percentageCapped() throws Exception {
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(1L, "CAP20", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(20), null, BigDecimal.valueOf(50), null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            BigDecimal discount = promoCodeService.calculateDiscount(pc, BigDecimal.valueOf(500));

            // 20% of 500 = 100, but capped at 50
            assertThat(discount).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("Fixed amount discount")
        void calculateDiscount_fixedAmount() throws Exception {
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(1L, "FLAT50", DiscountType.FIXED_AMOUNT,
                    BigDecimal.valueOf(50), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            BigDecimal discount = promoCodeService.calculateDiscount(pc, BigDecimal.valueOf(200));

            assertThat(discount).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("Discount never exceeds subtotal")
        void calculateDiscount_neverExceedsSubtotal() throws Exception {
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(1L, "BIG", DiscountType.FIXED_AMOUNT,
                    BigDecimal.valueOf(500), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            BigDecimal discount = promoCodeService.calculateDiscount(pc, BigDecimal.valueOf(200));

            // 500 fixed but subtotal is only 200
            assertThat(discount).isEqualByComparingTo("200.00");
        }

        @Test
        @DisplayName("Percentage discount — rounding to 2 decimal places")
        void calculateDiscount_percentageRounding() throws Exception {
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(1L, "ODD", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(15), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            BigDecimal discount = promoCodeService.calculateDiscount(pc, BigDecimal.valueOf(99));

            // 15% of 99 = 14.85
            assertThat(discount).isEqualByComparingTo("14.85");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // recordUsage()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("recordUsage()")
    class RecordUsageTests {

        @Test
        @DisplayName("Increments usedCount and saves usage record")
        void recordUsage_success() throws Exception {
            User user = buildUser(1L);
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(10L, "SAVE20", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(20), null, null, null, null, 3,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            Order order = Order.builder().build();
            setId(order, 99L);

            when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(inv -> inv.getArgument(0));
            when(promoCodeUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            promoCodeService.recordUsage(pc, user, order);

            assertThat(pc.getUsedCount()).isEqualTo(4);
            verify(promoCodeRepository).save(pc);
            verify(promoCodeUsageRepository).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // create() — Admin
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Success — creates a new promo code")
        void create_success() {
            Instant now = Instant.now();
            PromoCodeRequest request = buildRequest("NEWCODE", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, null, null,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS));

            when(promoCodeRepository.existsByCode("NEWCODE")).thenReturn(false);
            when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(inv -> {
                PromoCode pc = inv.getArgument(0);
                setId(pc, 1L);
                return pc;
            });

            PromoCodeResponse response = promoCodeService.create(request);

            assertThat(response.code()).isEqualTo("NEWCODE");
            assertThat(response.discountType()).isEqualTo(DiscountType.PERCENTAGE);
            assertThat(response.discountValue()).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("Fails — duplicate code")
        void create_duplicateCode() {
            Instant now = Instant.now();
            PromoCodeRequest request = buildRequest("DUP", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, null, null,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS));

            when(promoCodeRepository.existsByCode("DUP")).thenReturn(true);

            assertThatThrownBy(() -> promoCodeService.create(request))
                    .isInstanceOf(DuplicatePromoCodeException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // findAll() — Admin
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Returns list of promo codes")
        void findAll_success() throws Exception {
            Instant now = Instant.now();
            PromoCode pc1 = buildPromoCode(1L, "CODE1", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);
            PromoCode pc2 = buildPromoCode(2L, "CODE2", DiscountType.FIXED_AMOUNT,
                    BigDecimal.valueOf(25), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            Pageable pageable = PageRequest.of(0, 20);
            Page<PromoCode> page = new PageImpl<>(List.of(pc1, pc2), pageable, 2);
            when(promoCodeRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<PromoCodeResponse> result = promoCodeService.findAll(0, 20);

            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).code()).isEqualTo("CODE1");
            assertThat(result.content().get(1).code()).isEqualTo("CODE2");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // toggleActive() — Admin
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toggleActive()")
    class ToggleActiveTests {

        @Test
        @DisplayName("Toggles active from true to false")
        void toggleActive_deactivate() throws Exception {
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(1L, "CODE1", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), true);

            when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(pc));
            when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(inv -> inv.getArgument(0));

            PromoCodeResponse response = promoCodeService.toggleActive(1L);

            assertThat(response.active()).isFalse();
        }

        @Test
        @DisplayName("Toggles active from false to true")
        void toggleActive_activate() throws Exception {
            Instant now = Instant.now();
            PromoCode pc = buildPromoCode(1L, "CODE1", DiscountType.PERCENTAGE,
                    BigDecimal.valueOf(10), null, null, null, null, 0,
                    now.minus(1, ChronoUnit.DAYS), now.plus(30, ChronoUnit.DAYS), false);

            when(promoCodeRepository.findById(1L)).thenReturn(Optional.of(pc));
            when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(inv -> inv.getArgument(0));

            PromoCodeResponse response = promoCodeService.toggleActive(1L);

            assertThat(response.active()).isTrue();
        }

        @Test
        @DisplayName("Fails — promo code not found")
        void toggleActive_notFound() {
            when(promoCodeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> promoCodeService.toggleActive(99L))
                    .isInstanceOf(PromoCodeNotFoundException.class);
        }
    }
}
