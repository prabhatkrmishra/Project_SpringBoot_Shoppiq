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
import com.pkmprojects.shoppiq.controller.seller.SellerDashboardController;
import com.pkmprojects.shoppiq.dto.seller.response.SellerDashboardResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderItemResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.SellerSuspendedException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.seller.SellerDashboardService;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SellerDashboardController.class)
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
@DisplayName("SellerDashboardController Tests")
class SellerDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerDashboardService sellerDashboardService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = User.builder()
                .name("Seller User")
                .username("selleruser")
                .email("seller@example.com")
                .password("hashed")
                .enabled(true)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("GET /seller/dashboard/summary")
    class GetSummary {

        @Test
        @DisplayName("Returns 200 with dashboard summary")
        void getSummary_returnsData() throws Exception {
            when(sellerDashboardService.getDashboardSummary(any(User.class)))
                    .thenReturn(SellerDashboardResponse.from(10, 25, BigDecimal.valueOf(5000), 3, 1));

            mockMvc.perform(get("/seller/dashboard/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalProducts").value(10))
                    .andExpect(jsonPath("$.totalOrders").value(25))
                    .andExpect(jsonPath("$.totalRevenue").value(5000))
                    .andExpect(jsonPath("$.lowStockProducts").value(3))
                    .andExpect(jsonPath("$.outOfStockProducts").value(1));
        }

        @Test
        @DisplayName("Returns 404 when seller profile does not exist")
        void getSummary_sellerNotFound_returns404() throws Exception {
            when(sellerDashboardService.getDashboardSummary(any(User.class)))
                    .thenThrow(SellerNotFoundException.userId(1L));

            mockMvc.perform(get("/seller/dashboard/summary"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-404-001"));
        }

        @Test
        @DisplayName("Returns 400 when seller is suspended")
        void getSummary_sellerSuspended_returns400() throws Exception {
            when(sellerDashboardService.getDashboardSummary(any(User.class)))
                    .thenThrow(SellerSuspendedException.forAction(1L, "view dashboard"));

            mockMvc.perform(get("/seller/dashboard/summary"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-400-002"));
        }

        @Test
        @DisplayName("Returns 400 when seller is not verified")
        void getSummary_sellerNotVerified_returns400() throws Exception {
            when(sellerDashboardService.getDashboardSummary(any(User.class)))
                    .thenThrow(SellerNotVerifiedException.forAction(1L, "view dashboard"));

            mockMvc.perform(get("/seller/dashboard/summary"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-400-001"));
        }
    }

    @Nested
    @DisplayName("GET /seller/dashboard/recent-orders")
    class GetRecentOrders {

        @Test
        @DisplayName("Returns 200 with recent orders list")
        void getRecentOrders_returnsList() throws Exception {
            when(sellerDashboardService.getRecentOrders(any(User.class)))
                    .thenReturn(List.of(
                            new SellerOrderResponse(1L, OrderStatus.PLACED, PaymentMethod.ONLINE,
                                    PaymentStatus.PAID, BigDecimal.valueOf(100), BigDecimal.ZERO,
                                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(100),
                                    Instant.now(), Instant.now(), Instant.now(),
                                    List.of(new SellerOrderItemResponse(1L, "Product",
                                            BigDecimal.TEN, 2, BigDecimal.valueOf(20)))),
                            new SellerOrderResponse(2L, OrderStatus.SHIPPED, PaymentMethod.ONLINE,
                                    PaymentStatus.PAID, BigDecimal.valueOf(50), BigDecimal.ZERO,
                                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(50),
                                    Instant.now(), Instant.now(), Instant.now(),
                                    List.of(new SellerOrderItemResponse(2L, "Product 2",
                                            BigDecimal.valueOf(25), 2, BigDecimal.valueOf(50))))
                    ));

            mockMvc.perform(get("/seller/dashboard/recent-orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].status").value("PLACED"))
                    .andExpect(jsonPath("$[1].status").value("SHIPPED"));
        }

        @Test
        @DisplayName("Returns 200 with empty list when no orders")
        void getRecentOrders_emptyList() throws Exception {
            when(sellerDashboardService.getRecentOrders(any(User.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/seller/dashboard/recent-orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
