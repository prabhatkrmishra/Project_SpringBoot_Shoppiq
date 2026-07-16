package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.config.JacksonConfig;
import com.pkmprojects.shoppiq.config.SecurityConfig;
import com.pkmprojects.shoppiq.controller.admin.AdminProductController;
import com.pkmprojects.shoppiq.dto.admin.response.AdminProductResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;

import java.math.BigDecimal;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.admin.AdminProductService;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AdminProductController.class)
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
@ActiveProfiles("test")
@DisplayName("AdminProductController Tests")
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminProductService adminProductService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private static AdminProductResponse stubResponse(Long id, ProductPublishingStatus status) {
        return new AdminProductResponse(id, "Product " + id, "Description " + id, "SKU-" + id,
                "Brand", new BigDecimal("99.99"), 10, "Electronics",
                1L, "Test Seller", status);
    }

    @Nested
    @DisplayName("GET /api/admin/products/pending")
    class GetPendingProducts {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with pending products list")
        void getPendingProducts_returnsList() throws Exception {
            when(adminProductService.getPendingProducts(anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(
                            stubResponse(1L, ProductPublishingStatus.DRAFT),
                            stubResponse(2L, ProductPublishingStatus.DRAFT)
                    ), 0, 20, 2, 1, true, false));

            mockMvc.perform(get("/api/admin/products/pending?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].publishingStatus").value("DRAFT"))
                    .andExpect(jsonPath("$.content[1].publishingStatus").value("DRAFT"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with empty list when no pending products")
        void getPendingProducts_emptyList() throws Exception {
            when(adminProductService.getPendingProducts(anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 1, true, false));

            mockMvc.perform(get("/api/admin/products/pending?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Forwards to error page when not admin")
        void getPendingProducts_forbidden_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/products/pending?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/error"));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/products/{id}/publish")
    class PublishProduct {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with published product")
        void publishProduct_valid_returns200() throws Exception {
            when(adminProductService.publishProduct(1L))
                    .thenReturn(stubResponse(1L, ProductPublishingStatus.PUBLISHED));

            mockMvc.perform(put("/api/admin/products/1/publish").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemId").value(1))
                    .andExpect(jsonPath("$.publishingStatus").value("PUBLISHED"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 404 when product does not exist")
        void publishProduct_notFound_returns404() throws Exception {
            when(adminProductService.publishProduct(99L))
                    .thenThrow(ItemNotFoundException.id(99L));

            mockMvc.perform(put("/api/admin/products/99/publish").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-404-001"));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/products/{id}/reject")
    class RejectProduct {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with rejected product")
        void rejectProduct_valid_returns200() throws Exception {
            when(adminProductService.rejectProduct(1L))
                    .thenReturn(stubResponse(1L, ProductPublishingStatus.REJECTED));

            mockMvc.perform(put("/api/admin/products/1/reject").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemId").value(1))
                    .andExpect(jsonPath("$.publishingStatus").value("REJECTED"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 404 when product does not exist")
        void rejectProduct_notFound_returns404() throws Exception {
            when(adminProductService.rejectProduct(99L))
                    .thenThrow(ItemNotFoundException.id(99L));

            mockMvc.perform(put("/api/admin/products/99/reject").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-404-001"));
        }
    }
}
