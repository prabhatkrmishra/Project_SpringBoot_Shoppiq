package com.pkmprojects.shoppiq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.config.*;
import com.pkmprojects.shoppiq.controller.admin.AdminBannerController;
import com.pkmprojects.shoppiq.dto.banner.BannerRequest;
import com.pkmprojects.shoppiq.dto.banner.BannerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.entity.enums.BannerType;
import com.pkmprojects.shoppiq.exception.BannerNotFoundException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.BannerService;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminBannerController.class)
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
@DisplayName("AdminBannerController Tests")
class AdminBannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private BannerResponse bannerResponse;

    @BeforeEach
    void setUp() {
        bannerResponse = new BannerResponse(
                1L, "Limited Time", BannerType.PRIMARY, "Up to 50% Off",
                "Deals", "Shop Sale", "/sale", "#FFFFFF",
                "rgba(255,255,255,0.85)", 1, true, null, null
        );
    }

    private BannerRequest buildRequest() {
        return new BannerRequest(
                "Limited Time", BannerType.PRIMARY, "Up to 50% Off",
                "Deals", "Shop Sale", "/sale", "#FFFFFF",
                "rgba(255,255,255,0.85)", 1, true
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET /api/admin/banners
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/admin/banners")
    class GetAllTests {

        @Test
        @DisplayName("Returns paginated banners with ADMIN role")
        @WithMockUser(roles = "ADMIN")
        void findAll_admin() throws Exception {
            PageResponse<BannerResponse> pageResponse = new PageResponse<>(
                    List.of(bannerResponse), 0, 20, 1, 1, true, true
            );
            when(bannerService.findAll(0, 20)).thenReturn(pageResponse);

            mockMvc.perform(get("/api/admin/banners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].badgeText").value("Limited Time"))
                    .andExpect(jsonPath("$.content[0].badgeType").value("PRIMARY"));
        }

        @Test
        @DisplayName("Forwards to error page when not admin")
        @WithMockUser(roles = "CUSTOMER")
        void findAll_forbidden() throws Exception {
            mockMvc.perform(get("/api/admin/banners"))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/error"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET /api/admin/banners/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/admin/banners/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Returns banner by ID")
        @WithMockUser(roles = "ADMIN")
        void findById_success() throws Exception {
            when(bannerService.findById(1L)).thenReturn(bannerResponse);

            mockMvc.perform(get("/api/admin/banners/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.badgeText").value("Limited Time"));
        }

        @Test
        @DisplayName("Returns 404 when banner not found")
        @WithMockUser(roles = "ADMIN")
        void findById_notFound() throws Exception {
            when(bannerService.findById(99L))
                    .thenThrow(BannerNotFoundException.forId(99L));

            mockMvc.perform(get("/api/admin/banners/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POST /api/admin/banners
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/admin/banners")
    class CreateTests {

        @Test
        @DisplayName("Creates a new banner")
        @WithMockUser(roles = "ADMIN")
        void create_success() throws Exception {
            when(bannerService.create(any(BannerRequest.class))).thenReturn(bannerResponse);

            mockMvc.perform(post("/api/admin/banners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.badgeText").value("Limited Time"));
        }

        @Test
        @DisplayName("Returns 400 with invalid request body")
        @WithMockUser(roles = "ADMIN")
        void create_invalid() throws Exception {
            String invalidBody = """
                    {"badgeText": "", "heading": ""}
                    """;

            mockMvc.perform(post("/api/admin/banners")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUT /api/admin/banners/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/admin/banners/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Updates an existing banner")
        @WithMockUser(roles = "ADMIN")
        void update_success() throws Exception {
            BannerResponse updated = new BannerResponse(
                    1L, "Just In", BannerType.SECONDARY, "New Arrivals",
                    null, "Explore", "/new-arrivals", "#FFFFFF",
                    "rgba(255,255,255,0.85)", 2, true, null, null
            );
            when(bannerService.update(eq(1L), any(BannerRequest.class))).thenReturn(updated);

            mockMvc.perform(put("/api/admin/banners/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.badgeText").value("Just In"));
        }

        @Test
        @DisplayName("Returns 404 when banner not found")
        @WithMockUser(roles = "ADMIN")
        void update_notFound() throws Exception {
            when(bannerService.update(eq(99L), any(BannerRequest.class)))
                    .thenThrow(BannerNotFoundException.forId(99L));

            mockMvc.perform(put("/api/admin/banners/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isNotFound());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PATCH /api/admin/banners/{id}/toggle
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PATCH /api/admin/banners/{id}/toggle")
    class ToggleTests {

        @Test
        @DisplayName("Toggles banner active status")
        @WithMockUser(roles = "ADMIN")
        void toggle_success() throws Exception {
            BannerResponse toggled = new BannerResponse(
                    1L, "Limited Time", BannerType.PRIMARY, "Up to 50% Off",
                    null, null, null, "#FFFFFF", "rgba(255,255,255,0.85)",
                    1, false, null, null
            );
            when(bannerService.toggleActive(1L)).thenReturn(toggled);

            mockMvc.perform(patch("/api/admin/banners/1/toggle"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DELETE /api/admin/banners/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/admin/banners/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Deletes a banner")
        @WithMockUser(roles = "ADMIN")
        void delete_success() throws Exception {
            doNothing().when(bannerService).delete(1L);

            mockMvc.perform(delete("/api/admin/banners/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Returns 404 when banner not found")
        @WithMockUser(roles = "ADMIN")
        void delete_notFound() throws Exception {
            doThrow(BannerNotFoundException.forId(99L)).when(bannerService).delete(99L);

            mockMvc.perform(delete("/api/admin/banners/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
