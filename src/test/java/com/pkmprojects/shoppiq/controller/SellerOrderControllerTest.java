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
import com.pkmprojects.shoppiq.dto.common.PageResponse;
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
import static org.mockito.ArgumentMatchers.anyInt;
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
            when(sellerOrderService.getOrders(any(User.class), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(
                            List.of(
                                    stubResponse(1L, OrderStatus.PLACED),
                                    stubResponse(2L, OrderStatus.SHIPPED)
                            ),
                            0, 20, 2, 1, true, false
                    ));

            mockMvc.perform(get("/seller/orders?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("PLACED"))
                    .andExpect(jsonPath("$.content[1].status").value("SHIPPED"));
        }

        @Test
        @DisplayName("Returns 200 with empty list when no orders")
        void getOrders_emptyList() throws Exception {
            when(sellerOrderService.getOrders(any(User.class), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(
                            List.of(),
                            0, 20, 0, 1, true, false
                    ));

            mockMvc.perform(get("/seller/orders?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        @DisplayName("Returns 404 when seller profile does not exist")
        void getOrders_sellerNotFound_returns404() throws Exception {
            when(sellerOrderService.getOrders(any(User.class), anyInt(), anyInt()))
                    .thenThrow(SellerNotFoundException.userId(1L));

            mockMvc.perform(get("/seller/orders?page=0&size=20"))
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

        @Test
        @DisplayName("Returns 200 with RETURN_REQUEST status")
        void updateOrderStatus_returnRequest_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.RETURN_REQUEST)))
                    .thenReturn(stubResponse(1L, OrderStatus.RETURN_REQUEST));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "RETURN_REQUEST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RETURN_REQUEST"));
        }

        @Test
        @DisplayName("Returns 200 with RETURN_PICKUP status")
        void updateOrderStatus_returnPickup_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.RETURN_PICKUP)))
                    .thenReturn(stubResponse(1L, OrderStatus.RETURN_PICKUP));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "RETURN_PICKUP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RETURN_PICKUP"));
        }

        @Test
        @DisplayName("Returns 200 with REFUND_REQUEST status")
        void updateOrderStatus_refundRequest_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.REFUND_REQUEST)))
                    .thenReturn(stubResponse(1L, OrderStatus.REFUND_REQUEST));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "REFUND_REQUEST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUND_REQUEST"));
        }

        @Test
        @DisplayName("Returns 200 with REPLACE_REQUEST status")
        void updateOrderStatus_replaceRequest_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.REPLACE_REQUEST)))
                    .thenReturn(stubResponse(1L, OrderStatus.REPLACE_REQUEST));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "REPLACE_REQUEST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REPLACE_REQUEST"));
        }

        @Test
        @DisplayName("Returns 200 with REPLACE_PICKUP status")
        void updateOrderStatus_replacePickup_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.REPLACE_PICKUP)))
                    .thenReturn(stubResponse(1L, OrderStatus.REPLACE_PICKUP));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "REPLACE_PICKUP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REPLACE_PICKUP"));
        }

        @Test
        @DisplayName("Returns 400 on invalid transition")
        void updateOrderStatus_invalidTransition_returns400() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.DELIVERED)))
                    .thenThrow(new com.pkmprojects.shoppiq.exception.OrderInvalidStatusTransitionException(
                            OrderStatus.CONFIRMED, OrderStatus.DELIVERED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "DELIVERED"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("ORDER-400-002"));
        }

        @Test
        @DisplayName("Returns 404 when order not found on status update")
        void updateOrderStatus_orderNotFound_returns404() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(99L), eq(OrderStatus.CONFIRMED)))
                    .thenThrow(OrderNotFoundException.id(99L));

            mockMvc.perform(put("/seller/orders/99/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ORDER-404-001"));
        }

        @Test
        @DisplayName("Returns 400 when seller not found on status update")
        void updateOrderStatus_sellerNotFound_returns404() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CONFIRMED)))
                    .thenThrow(SellerNotFoundException.userId(1L));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-404-001"));
        }

        // ─── Full lifecycle transitions ─────────────────────────────────

        @Test
        @DisplayName("Returns 200 — PLACED → CONFIRMED")
        void updateOrderStatus_placedToConfirmed_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CONFIRMED)))
                    .thenReturn(stubResponse(1L, OrderStatus.CONFIRMED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("Returns 200 — CONFIRMED → SHIPPED")
        void updateOrderStatus_confirmedToShipped_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.SHIPPED)))
                    .thenReturn(stubResponse(1L, OrderStatus.SHIPPED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "SHIPPED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SHIPPED"));
        }

        @Test
        @DisplayName("Returns 200 — SHIPPED → OUT_FOR_DELIVERY")
        void updateOrderStatus_shippedToOutForDelivery_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.OUT_FOR_DELIVERY)))
                    .thenReturn(stubResponse(1L, OrderStatus.OUT_FOR_DELIVERY));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "OUT_FOR_DELIVERY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OUT_FOR_DELIVERY"));
        }

        @Test
        @DisplayName("Returns 200 — OUT_FOR_DELIVERY → DELIVERED")
        void updateOrderStatus_outForDeliveryToDelivered_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.DELIVERED)))
                    .thenReturn(stubResponse(1L, OrderStatus.DELIVERED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "DELIVERED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DELIVERED"));
        }

        // ─── Post-delivery request flow transitions ──────────────────────

        @Test
        @DisplayName("Returns 200 — DELIVERED → RETURN_REQUEST")
        void updateOrderStatus_deliveredToReturnRequest_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.RETURN_REQUEST)))
                    .thenReturn(stubResponse(1L, OrderStatus.RETURN_REQUEST));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "RETURN_REQUEST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RETURN_REQUEST"));
        }

        @Test
        @DisplayName("Returns 200 — DELIVERED → REFUND_REQUEST")
        void updateOrderStatus_deliveredToRefundRequest_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.REFUND_REQUEST)))
                    .thenReturn(stubResponse(1L, OrderStatus.REFUND_REQUEST));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "REFUND_REQUEST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUND_REQUEST"));
        }

        @Test
        @DisplayName("Returns 200 — DELIVERED → REPLACE_REQUEST")
        void updateOrderStatus_deliveredToReplaceRequest_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.REPLACE_REQUEST)))
                    .thenReturn(stubResponse(1L, OrderStatus.REPLACE_REQUEST));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "REPLACE_REQUEST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REPLACE_REQUEST"));
        }

        @Test
        @DisplayName("Returns 200 — RETURN_PICKUP → RETURNED")
        void updateOrderStatus_returnPickupToReturned_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.RETURNED)))
                    .thenReturn(stubResponse(1L, OrderStatus.RETURNED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "RETURNED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RETURNED"));
        }

        @Test
        @DisplayName("Returns 200 — RETURN_PICKUP → REFUNDED")
        void updateOrderStatus_returnPickupToRefunded_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.REFUNDED)))
                    .thenReturn(stubResponse(1L, OrderStatus.REFUNDED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "REFUNDED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUNDED"));
        }

        @Test
        @DisplayName("Returns 200 — REPLACE_PICKUP → REPLACED")
        void updateOrderStatus_replacePickupToReplaced_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.REPLACED)))
                    .thenReturn(stubResponse(1L, OrderStatus.REPLACED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "REPLACED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REPLACED"));
        }

        // ─── Cancellation transitions ────────────────────────────────────

        @Test
        @DisplayName("Returns 200 — PLACED → CANCEL_REQUEST")
        void updateOrderStatus_placedToCancelRequest_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CANCEL_REQUEST)))
                    .thenReturn(stubResponse(1L, OrderStatus.CANCEL_REQUEST));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CANCEL_REQUEST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCEL_REQUEST"));
        }

        @Test
        @DisplayName("Returns 200 — PLACED → CANCELLED (direct)")
        void updateOrderStatus_placedToCancelled_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CANCELLED)))
                    .thenReturn(stubResponse(1L, OrderStatus.CANCELLED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CANCELLED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("Returns 200 — CANCEL_REQUEST → CANCELLED")
        void updateOrderStatus_cancelRequestToCancelled_returns200() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CANCELLED)))
                    .thenReturn(stubResponse(1L, OrderStatus.CANCELLED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CANCELLED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        // ─── Unauthenticated access ──────────────────────────────────────

        @Test
        @DisplayName("401 Unauthorized — status update without auth")
        void updateOrderStatus_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isUnauthorized());
        }

        // ─── Invalid cross-flow transitions ──────────────────────────────

        @Test
        @DisplayName("Returns 400 — PLACED → DELIVERED (skip flow)")
        void updateOrderStatus_placedToDelivered_returns400() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.DELIVERED)))
                    .thenThrow(new com.pkmprojects.shoppiq.exception.OrderInvalidStatusTransitionException(
                            OrderStatus.PLACED, OrderStatus.DELIVERED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "DELIVERED"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 400 — CONFIRMED → CANCELLED")
        void updateOrderStatus_confirmedToCancelled_returns400() throws Exception {
            when(sellerOrderService.updateOrderStatus(any(User.class), eq(1L), eq(OrderStatus.CANCELLED)))
                    .thenThrow(new com.pkmprojects.shoppiq.exception.OrderInvalidStatusTransitionException(
                            OrderStatus.CONFIRMED, OrderStatus.CANCELLED));

            mockMvc.perform(put("/seller/orders/1/status")
                            .param("status", "CANCELLED"))
                    .andExpect(status().isBadRequest());
        }
    }
}
