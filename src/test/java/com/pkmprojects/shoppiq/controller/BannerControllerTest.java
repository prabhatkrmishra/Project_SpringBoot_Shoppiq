package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.config.*;
import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.entity.enums.BannerType;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.BannerService;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BannerController.class)
@Import({
        SecurityConfig.class,
        JacksonConfig.class,
        GlobalExceptionHandler.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationUtils.class,
        JwtCookieFactory.class,
        ShoppiqAuthenticationEntryPoint.class,
        ShoppiqAccessDeniedHandler.class,
        ProblemDetailResponseWriter.class
})
@DisplayName("BannerController Tests")
class BannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BannerService bannerService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private List<BannerResponse> activeBanners;

    @BeforeEach
    void setUp() {
        activeBanners = List.of(
                new BannerResponse(1L, "Limited Time", BannerType.PRIMARY,
                        "Up to 50% Off", "Deals", "Shop Sale", "/sale",
                        "#FFFFFF", "rgba(255,255,255,0.85)", 1, true, null, null),
                new BannerResponse(2L, "Just In", BannerType.SECONDARY,
                        "New Arrivals", "Fresh drops", "Explore", "/new-arrivals",
                        "#FFFFFF", "rgba(255,255,255,0.85)", 2, true, null, null),
                new BannerResponse(3L, "Perks", BannerType.ACCENT,
                        "Free Shipping", null, null, null,
                        "#FFFFFF", "rgba(255,255,255,0.85)", 3, true, null, null)
        );
    }

    @Test
    @DisplayName("GET /api/banners/active returns active banners without auth")
    void getActiveBanners_success() throws Exception {
        when(bannerService.findAllActive()).thenReturn(activeBanners);

        mockMvc.perform(get("/api/banners/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].badgeText").value("Limited Time"))
                .andExpect(jsonPath("$[0].badgeType").value("PRIMARY"))
                .andExpect(jsonPath("$[1].badgeText").value("Just In"))
                .andExpect(jsonPath("$[1].badgeType").value("SECONDARY"))
                .andExpect(jsonPath("$[2].badgeText").value("Perks"))
                .andExpect(jsonPath("$[2].badgeType").value("ACCENT"));
    }

    @Test
    @DisplayName("GET /api/banners/active returns empty array when no banners")
    void getActiveBanners_empty() throws Exception {
        when(bannerService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get("/api/banners/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
