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
import com.pkmprojects.shoppiq.controller.admin.AdminSellerController;
import com.pkmprojects.shoppiq.dto.admin.response.AdminSellerResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.SellerApprovalInvalidException;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.admin.AdminSellerService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AdminSellerController.class)
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
@DisplayName("AdminSellerController Tests")
class AdminSellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminSellerService adminSellerService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private static AdminSellerResponse stubResponse(Long id, VerificationStatus vStatus) {
        return new AdminSellerResponse(
                id, 1L, "User " + id, "user" + id + "@test.com",
                "Business " + id, "business" + id + "@test.com",
                "9999999999", "GST" + id, "PAN" + id,
                vStatus, SellerStatus.ACTIVE,
                BigDecimal.valueOf(5.0), BigDecimal.valueOf(4.5),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("GET /api/admin/sellers")
    class GetSellers {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with all sellers when no status filter")
        void getSellers_noFilter_returnsAll() throws Exception {
            when(adminSellerService.getAllSellers(anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(
                            stubResponse(1L, VerificationStatus.PENDING),
                            stubResponse(2L, VerificationStatus.APPROVED)
                    ), 0, 20, 2, 1, true, false));

            mockMvc.perform(get("/api/admin/sellers?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].verificationStatus").value("PENDING"))
                    .andExpect(jsonPath("$.content[1].verificationStatus").value("APPROVED"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 filtered by status")
        void getSellers_withStatusFilter_returnsFiltered() throws Exception {
            when(adminSellerService.getSellersByStatus(eq(VerificationStatus.PENDING), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(stubResponse(1L, VerificationStatus.PENDING)), 0, 20, 1, 1, true, false));

            mockMvc.perform(get("/api/admin/sellers?status=PENDING&page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].verificationStatus").value("PENDING"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with empty list when no sellers exist")
        void getSellers_emptyList() throws Exception {
            when(adminSellerService.getAllSellers(anyInt(), anyInt())).thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 1, true, false));

            mockMvc.perform(get("/api/admin/sellers?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Forwards to error page when not admin")
        void getSellers_forbidden_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/sellers"))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/error"));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/sellers/{id}/approve")
    class ApproveSeller {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with approved seller")
        void approveSeller_valid_returns200() throws Exception {
            when(adminSellerService.approveSeller(1L))
                    .thenReturn(stubResponse(1L, VerificationStatus.APPROVED));

            mockMvc.perform(put("/api/admin/sellers/1/approve").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.verificationStatus").value("APPROVED"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 404 when seller not found")
        void approveSeller_notFound_returns404() throws Exception {
            when(adminSellerService.approveSeller(99L))
                    .thenThrow(SellerNotFoundException.id(99L));

            mockMvc.perform(put("/api/admin/sellers/99/approve").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-404-001"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 400 when seller not in PENDING status")
        void approveSeller_notPending_returns400() throws Exception {
            when(adminSellerService.approveSeller(1L))
                    .thenThrow(SellerApprovalInvalidException.notPending(1L));

            mockMvc.perform(put("/api/admin/sellers/1/approve").with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYSTEM-400-001"));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/sellers/{id}/reject")
    class RejectSeller {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with rejected seller")
        void rejectSeller_valid_returns200() throws Exception {
            when(adminSellerService.rejectSeller(1L))
                    .thenReturn(stubResponse(1L, VerificationStatus.REJECTED));

            mockMvc.perform(put("/api/admin/sellers/1/reject").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.verificationStatus").value("REJECTED"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 404 when seller not found")
        void rejectSeller_notFound_returns404() throws Exception {
            when(adminSellerService.rejectSeller(99L))
                    .thenThrow(SellerNotFoundException.id(99L));

            mockMvc.perform(put("/api/admin/sellers/99/reject").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-404-001"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 400 when seller not in PENDING status")
        void rejectSeller_notPending_returns400() throws Exception {
            when(adminSellerService.rejectSeller(1L))
                    .thenThrow(SellerApprovalInvalidException.notPending(1L));

            mockMvc.perform(put("/api/admin/sellers/1/reject").with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYSTEM-400-001"));
        }
    }
}
