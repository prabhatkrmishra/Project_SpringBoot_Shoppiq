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
import com.pkmprojects.shoppiq.controller.seller.SellerOrderController;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderItemResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerOrderResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.OrderNotFoundException;
import com.pkmprojects.shoppiq.exception.OrderNotFullyOwnedException;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.SellerSuspendedException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.seller.SellerOrderService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SellerOrderController.class)
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
@DisplayName("SellerOrderController Tests")
class SellerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerOrderService sellerOrderService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private static SellerOrderResponse stubResponse(Long id, OrderStatus status) {
        return new SellerOrderResponse(
                id, status, PaymentMethod.ONLINE, PaymentStatus.PAID,
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(100), Instant.now(), Instant.now(), Instant.now(),
                List.of(new SellerOrderItemResponse(1L, "Product", BigDecimal.TEN, 2, BigDecimal.valueOf(20)))
        );
    }

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
    @DisplayName("GET /seller/orders")
    class GetOrders {

        @Test
        @DisplayName("Returns 200 with order list")
        void getOrders_returnsList() throws Exception {
            when(sellerOrderService.getOrders(any(User.class)))
                    .thenReturn(List.of(
                            stubResponse(1L, OrderStatus.PLACED),
                            stubResponse(2L, OrderStatus.SHIPPED)
                    ));

            mockMvc.perform(get("/seller/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].status").value("PLACED"))
                    .andExpect(jsonPath("$[1].status").value("SHIPPED"));
        }

        @Test
        @DisplayName("Returns 200 with empty list when no orders")
        void getOrders_emptyList() throws Exception {
            when(sellerOrderService.getOrders(any(User.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/seller/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Returns 404 when seller profile does not exist")
        void getOrders_sellerNotFound_returns404() throws Exception {
            when(sellerOrderService.getOrders(any(User.class)))
                    .thenThrow(SellerNotFoundException.userId(1L));

            mockMvc.perform(get("/seller/orders"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-404-001"));
        }
    }

    @Nested
    @DisplayName("GET /seller/orders/{id}")
    class GetOrder {

        @Test
        @DisplayName("Returns 200 with order details")
        void getOrder_returnsOrder() throws Exception {
            when(sellerOrderService.getOrder(any(User.class), eq(1L)))
                    .thenReturn(stubResponse(1L, OrderStatus.PLACED));

            mockMvc.perform(get("/seller/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("PLACED"))
                    .andExpect(jsonPath("$.items.length()").value(1));
        }

        @Test
        @DisplayName("Returns 404 when order does not exist")
        void getOrder_notFound_returns404() throws Exception {
            when(sellerOrderService.getOrder(any(User.class), eq(99L)))
                    .thenThrow(OrderNotFoundException.id(99L));

            mockMvc.perform(get("/seller/orders/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ORDER-404-001"));
        }
    }

    @Nested
    @DisplayName("PUT /seller/orders/{id}/status")
    class UpdateOrderStatus {

        @Test
        @DisplayName("Returns 200 with updated order on success")
        void updateOrderStatus_valid_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CONFIRMED)))
                    .thenReturn(stubResponse(1L, OrderStatus.CONFIRMED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("Returns 400 when order has items from other sellers")
        void updateOrderStatus_notFullyOwned_returns400() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CONFIRMED)))
                    .thenThrow(OrderNotFullyOwnedException.forOrder(1L));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SYSTEM-400-001"));
        }

        @Test
        @DisplayName("Returns 404 when order does not exist")
        void updateOrderStatus_notFound_returns404() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(99L), eq(OrderStatus.CONFIRMED)))
                    .thenThrow(OrderNotFoundException.id(99L));

            mockMvc.perform(put("/seller/orders/99/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ORDER-404-001"));
        }

        @Test
        @DisplayName("Returns 400 when seller is suspended")
        void updateOrderStatus_sellerSuspended_returns400() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CONFIRMED)))
                    .thenThrow(SellerSuspendedException.forAction(1L, "manage orders"));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-400-002"));
        }

        @Test
        @DisplayName("Returns 400 when seller is not verified")
        void updateOrderStatus_sellerNotVerified_returns400() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CONFIRMED)))
                    .thenThrow(SellerNotVerifiedException.forAction(1L, "manage orders"));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-400-001"));
        }

        @Test
        @DisplayName("Returns 400 when status parameter is missing")
        void updateOrderStatus_missingStatus_returns400() throws Exception {
            mockMvc.perform(put("/seller/orders/1/status"))
                    .andExpect(status().isBadRequest());

            verify(sellerOrderService, never()).updateOrderStatus(any(), anyLong(), any());
        }
    }
}
