package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.banner.BannerRequest;
import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.Banner;
import com.pkmprojects.shoppiq.entity.enums.BannerType;
import com.pkmprojects.shoppiq.exception.BannerNotFoundException;
import com.pkmprojects.shoppiq.repository.BannerRepository;
import com.pkmprojects.shoppiq.service.impl.BannerServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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
 * Unit tests for {@link BannerServiceImpl}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BannerServiceImpl Tests")
class BannerServiceImplTest {

    @Mock
    private BannerRepository bannerRepository;

    @InjectMocks
    private BannerServiceImpl bannerService;

    // ─── Helpers ──────────────────────────────────────────────────────────

    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private Banner buildBanner(Long id, String badgeText, BannerType badgeType,
                                String heading, String bodyText,
                                String buttonText, String buttonLink,
                                Integer displayOrder, boolean active) throws Exception {
        Banner banner = Banner.builder()
                .badgeText(badgeText)
                .badgeType(badgeType)
                .heading(heading)
                .bodyText(bodyText)
                .buttonText(buttonText)
                .buttonLink(buttonLink)
                .headingColor("#FFFFFF")
                .bodyColor("rgba(255,255,255,0.85)")
                .displayOrder(displayOrder)
                .active(active)
                .build();
        setId(banner, id);
        return banner;
    }

    private BannerRequest buildRequest(String badgeText, BannerType badgeType,
                                        String heading, String bodyText,
                                        String buttonText, String buttonLink,
                                        Integer displayOrder, Boolean active) {
        return new BannerRequest(
                badgeText, badgeType, heading, bodyText,
                buttonText, buttonLink, "#FFFFFF",
                "rgba(255,255,255,0.85)", displayOrder, active
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // findAllActive()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAllActive()")
    class FindAllActiveTests {

        @Test
        @DisplayName("Returns list of active banners sorted by display order")
        void findAllActive_success() throws Exception {
            Banner b1 = buildBanner(1L, "Limited Time", BannerType.PRIMARY,
                    "Up to 50% Off", "Deals", "Shop Sale", "/sale", 1, true);
            Banner b2 = buildBanner(2L, "Just In", BannerType.SECONDARY,
                    "New Arrivals", "Fresh drops", "Explore", "/new-arrivals", 2, true);

            when(bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc())
                    .thenReturn(List.of(b1, b2));

            List<BannerResponse> result = bannerService.findAllActive();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).badgeText()).isEqualTo("Limited Time");
            assertThat(result.get(0).badgeType()).isEqualTo(BannerType.PRIMARY);
            assertThat(result.get(1).badgeText()).isEqualTo("Just In");
        }

        @Test
        @DisplayName("Returns empty list when no active banners")
        void findAllActive_empty() {
            when(bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc())
                    .thenReturn(List.of());

            List<BannerResponse> result = bannerService.findAllActive();

            assertThat(result).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // findAll(page, size)
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAll(page, size)")
    class FindAllPagedTests {

        @Test
        @DisplayName("Returns paginated banners")
        void findAll_success() throws Exception {
            Banner b1 = buildBanner(1L, "Limited Time", BannerType.PRIMARY,
                    "Up to 50% Off", null, null, null, 1, true);
            Banner b2 = buildBanner(2L, "Just In", BannerType.SECONDARY,
                    "New Arrivals", null, null, null, 2, true);

            Pageable pageable = PageRequest.of(0, 20);
            Page<Banner> page = new PageImpl<>(List.of(b1, b2), pageable, 2);
            when(bannerRepository.findAll(any(Pageable.class))).thenReturn(page);

            PageResponse<BannerResponse> result = bannerService.findAll(0, 20);

            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).badgeText()).isEqualTo("Limited Time");
            assertThat(result.content().get(1).badgeText()).isEqualTo("Just In");
            assertThat(result.totalElements()).isEqualTo(2);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // findById()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Returns banner by ID")
        void findById_success() throws Exception {
            Banner b = buildBanner(1L, "Limited Time", BannerType.PRIMARY,
                    "Up to 50% Off", "Deals", "Shop Sale", "/sale", 1, true);

            when(bannerRepository.findById(1L)).thenReturn(Optional.of(b));

            BannerResponse response = bannerService.findById(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.badgeText()).isEqualTo("Limited Time");
            assertThat(response.heading()).isEqualTo("Up to 50% Off");
        }

        @Test
        @DisplayName("Fails — banner not found")
        void findById_notFound() {
            when(bannerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bannerService.findById(99L))
                    .isInstanceOf(BannerNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // create()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Success — creates a new banner")
        void create_success() {
            BannerRequest request = buildRequest("Limited Time", BannerType.PRIMARY,
                    "Up to 50% Off", "Deals", "Shop Sale", "/sale", 1, true);

            when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> {
                Banner b = inv.getArgument(0);
                setId(b, 1L);
                return b;
            });

            BannerResponse response = bannerService.create(request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.badgeText()).isEqualTo("Limited Time");
            assertThat(response.badgeType()).isEqualTo(BannerType.PRIMARY);
            assertThat(response.heading()).isEqualTo("Up to 50% Off");
            assertThat(response.bodyText()).isEqualTo("Deals");
            assertThat(response.buttonText()).isEqualTo("Shop Sale");
            assertThat(response.buttonLink()).isEqualTo("/sale");
            assertThat(response.active()).isTrue();
        }

        @Test
        @DisplayName("Success — creates banner with null optional fields")
        void create_nullableFields() {
            BannerRequest request = buildRequest("Perks", BannerType.ACCENT,
                    "Free Shipping", null, null, null, 3, true);

            when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> {
                Banner b = inv.getArgument(0);
                setId(b, 1L);
                return b;
            });

            BannerResponse response = bannerService.create(request);

            assertThat(response.badgeText()).isEqualTo("Perks");
            assertThat(response.bodyText()).isNull();
            assertThat(response.buttonText()).isNull();
            assertThat(response.buttonLink()).isNull();
        }

        @Test
        @DisplayName("Success — applies defaults for null displayOrder and active")
        void create_defaults() {
            BannerRequest request = new BannerRequest(
                    "Test", BannerType.PRIMARY, "Heading",
                    null, null, null, null, null, null, null
            );

            when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> {
                Banner b = inv.getArgument(0);
                setId(b, 1L);
                return b;
            });

            BannerResponse response = bannerService.create(request);

            assertThat(response.displayOrder()).isEqualTo(0);
            assertThat(response.active()).isTrue();
            assertThat(response.headingColor()).isEqualTo("#FFFFFF");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // update()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Success — updates all banner fields")
        void update_success() throws Exception {
            Banner existing = buildBanner(1L, "Old Badge", BannerType.PRIMARY,
                    "Old Heading", "Old body", "Old", "/old", 1, true);

            when(bannerRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));

            BannerRequest request = buildRequest("New Badge", BannerType.SECONDARY,
                    "New Heading", "New body", "New", "/new", 5, false);

            BannerResponse response = bannerService.update(1L, request);

            assertThat(response.badgeText()).isEqualTo("New Badge");
            assertThat(response.badgeType()).isEqualTo(BannerType.SECONDARY);
            assertThat(response.heading()).isEqualTo("New Heading");
            assertThat(response.bodyText()).isEqualTo("New body");
            assertThat(response.buttonText()).isEqualTo("New");
            assertThat(response.buttonLink()).isEqualTo("/new");
            assertThat(response.displayOrder()).isEqualTo(5);
            assertThat(response.active()).isFalse();
        }

        @Test
        @DisplayName("Fails — banner not found")
        void update_notFound() {
            when(bannerRepository.findById(99L)).thenReturn(Optional.empty());

            BannerRequest request = buildRequest("X", BannerType.PRIMARY,
                    "X", null, null, null, 0, true);

            assertThatThrownBy(() -> bannerService.update(99L, request))
                    .isInstanceOf(BannerNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // toggleActive()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toggleActive()")
    class ToggleActiveTests {

        @Test
        @DisplayName("Toggles active from true to false")
        void toggleActive_deactivate() throws Exception {
            Banner b = buildBanner(1L, "Test", BannerType.PRIMARY,
                    "Heading", null, null, null, 1, false);

            when(bannerRepository.toggleActive(1L)).thenReturn(1);
            when(bannerRepository.findById(1L)).thenReturn(Optional.of(b));

            BannerResponse response = bannerService.toggleActive(1L);

            assertThat(response.active()).isFalse();
            verify(bannerRepository).toggleActive(1L);
            verify(bannerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Toggles active from false to true")
        void toggleActive_activate() throws Exception {
            Banner b = buildBanner(1L, "Test", BannerType.PRIMARY,
                    "Heading", null, null, null, 1, true);

            when(bannerRepository.toggleActive(1L)).thenReturn(1);
            when(bannerRepository.findById(1L)).thenReturn(Optional.of(b));

            BannerResponse response = bannerService.toggleActive(1L);

            assertThat(response.active()).isTrue();
        }

        @Test
        @DisplayName("Fails — banner not found")
        void toggleActive_notFound() {
            when(bannerRepository.toggleActive(99L)).thenReturn(0);

            assertThatThrownBy(() -> bannerService.toggleActive(99L))
                    .isInstanceOf(BannerNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // delete()
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Success — deletes a banner")
        void delete_success() throws Exception {
            Banner b = buildBanner(1L, "Test", BannerType.PRIMARY,
                    "Heading", null, null, null, 1, true);

            when(bannerRepository.findById(1L)).thenReturn(Optional.of(b));
            doNothing().when(bannerRepository).delete(b);

            bannerService.delete(1L);

            verify(bannerRepository).delete(b);
        }

        @Test
        @DisplayName("Fails — banner not found")
        void delete_notFound() {
            when(bannerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bannerService.delete(99L))
                    .isInstanceOf(BannerNotFoundException.class);

            verify(bannerRepository, never()).delete(any());
        }
    }
}
