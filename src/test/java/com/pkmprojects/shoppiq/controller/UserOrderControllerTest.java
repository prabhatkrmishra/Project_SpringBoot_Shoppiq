package com.pkmprojects.shoppiq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.oauth2.OAuthReturnUrlFilter;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.config.JacksonConfig;
import com.pkmprojects.shoppiq.config.SecurityConfig;
import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutRequest;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutResponse;
import com.pkmprojects.shoppiq.dto.order.OrderResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.enums.PaymentMethod;
import com.pkmprojects.shoppiq.enums.PaymentStatus;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.impl.CheckoutServiceImpl;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Controller-slice integration tests for {@link UserOrderController}.
 *
 * <p>
 * Uses {@code @WebMvcTest} with the real security filter chain imported.
 * {@link CheckoutServiceImpl} is mocked via {@code @MockitoBean}.
 * The authenticated {@link User} principal is injected directly into the
 * {@link SecurityContextHolder} to bypass the JWT filter.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@WebMvcTest(UserOrderController.class)
@Import({
        SecurityConfig.class,
        JacksonConfig.class,
        GlobalExceptionHandler.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationUtils.class,
        JwtCookieFactory.class,
        ShoppiqAuthenticationEntryPoint.class,
        ShoppiqAccessDeniedHandler.class,
        ProblemDetailResponseWriter.class,
        OAuthReturnUrlFilter.class
})
@DisplayName("UserOrderController Tests")
class UserOrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CheckoutServiceImpl checkoutService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean RolesService rolesService;
    @MockitoBean HttpCookieOAuth2AuthorizationRequestRepository cookieRepo;
    @MockitoBean OAuth2SuccessHandler oAuth2SuccessHandler;

    private User customer;

    // ─── Helpers ──────────────────────────────────────────────────────────

    private static void setId(Object entity, Long id) throws Exception {
        Field f = entity.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }

    private void authenticateCustomer() throws Exception {
        customer = User.builder()
                .name("Alice").username("alice")
                .email("alice@test.com").password("hashed")
                .enabled(true).build();
        setId(customer, 1L);

        var auth = new UsernamePasswordAuthenticationToken(
                customer, null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private CheckoutResponse checkoutResponse(Long orderId) {
        return new CheckoutResponse(orderId, OrderStatus.PLACED, BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.valueOf(500), 99L, null);
    }

    private OrderResponse orderResponse(Long orderId) {
        AddressResponse addr = new AddressResponse(
                1L, "Home", "Alice", "9999999999",
                "123 St", null, "Mumbai", "MH", "400001", "India",
                false, Instant.now(), Instant.now()
        );
        return new OrderResponse(
                orderId, OrderStatus.PLACED, PaymentMethod.COD, PaymentStatus.PENDING,
                addr,
                BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(500), Instant.now(), Instant.now(), Instant.now(),
                List.of()
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POST /user/order/checkout
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /user/order/checkout")
    class CheckoutEndpointTests {

        @Test
        @DisplayName("201 Created — successful checkout")
        void checkout_success() throws Exception {
            authenticateCustomer();
            CheckoutRequest request = new CheckoutRequest(1L, PaymentMethod.COD, null);
            when(checkoutService.checkout(eq(customer), any(CheckoutRequest.class)))
                    .thenReturn(checkoutResponse(25L));

            mockMvc.perform(post("/user/order/checkout").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderId").value(25))
                    .andExpect(jsonPath("$.status").value("PLACED"))
                    .andExpect(jsonPath("$.grandTotal").value(500.0));
        }

        @Test
        @DisplayName("400 Bad Request — empty cart")
        void checkout_emptyCart() throws Exception {
            authenticateCustomer();
            CheckoutRequest request = new CheckoutRequest(1L, PaymentMethod.COD, null);
            when(checkoutService.checkout(any(), any())).thenThrow(new CartEmptyException());

            mockMvc.perform(post("/user/order/checkout").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404 Not Found — invalid address")
        void checkout_invalidAddress() throws Exception {
            authenticateCustomer();
            CheckoutRequest request = new CheckoutRequest(99L, PaymentMethod.COD, null);
            when(checkoutService.checkout(any(), any()))
                    .thenThrow(AddressNotFoundException.id(99L));

            mockMvc.perform(post("/user/order/checkout").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — address belongs to different user")
        void checkout_addressWrongOwner() throws Exception {
            authenticateCustomer();
            CheckoutRequest request = new CheckoutRequest(5L, PaymentMethod.COD, null);
            when(checkoutService.checkout(any(), any()))
                    .thenThrow(AddressAccessDeniedException.forAddress(5L));

            mockMvc.perform(post("/user/order/checkout").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 Bad Request — insufficient stock")
        void checkout_insufficientStock() throws Exception {
            authenticateCustomer();
            CheckoutRequest request = new CheckoutRequest(1L, PaymentMethod.COD, null);
            when(checkoutService.checkout(any(), any()))
                    .thenThrow(InsufficientStockException.forItem("SKU-1", 3, 1));

            mockMvc.perform(post("/user/order/checkout").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — missing addressId")
        void checkout_validation_missingAddressId() throws Exception {
            authenticateCustomer();
            mockMvc.perform(post("/user/order/checkout").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"paymentMethod\":\"COD\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("401 Unauthorized — unauthenticated request")
        void checkout_unauthenticated() throws Exception {
            mockMvc.perform(post("/user/order/checkout").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"addressId\":1,\"paymentMethod\":\"COD\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET /user/order/get/all
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /user/order/get/all")
    class GetMyOrdersTests {

        @Test
        @DisplayName("200 OK — returns list of orders")
        void getMyOrders_success() throws Exception {
            authenticateCustomer();
            when(checkoutService.getMyOrders(eq(customer), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(orderResponse(1L), orderResponse(2L)), 0, 20, 2, 1, true, false));

            mockMvc.perform(get("/user/order/get/all?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[1].id").value(2));
        }

        @Test
        @DisplayName("200 OK — empty list when no orders")
        void getMyOrders_empty() throws Exception {
            authenticateCustomer();
            when(checkoutService.getMyOrders(eq(customer), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 1, true, true));

            mockMvc.perform(get("/user/order/get/all?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET /user/order/get/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /user/order/get/{id}")
    class GetMyOrderTests {

        @Test
        @DisplayName("200 OK — returns single order")
        void getMyOrder_success() throws Exception {
            authenticateCustomer();
            when(checkoutService.getMyOrder(customer, 10L)).thenReturn(orderResponse(10L));

            mockMvc.perform(get("/user/order/get/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.status").value("PLACED"));
        }

        @Test
        @DisplayName("404 Not Found — order does not exist")
        void getMyOrder_notFound() throws Exception {
            authenticateCustomer();
            when(checkoutService.getMyOrder(any(), eq(99L)))
                    .thenThrow(new OrderNotFoundException("Order '99' not found."));

            mockMvc.perform(get("/user/order/get/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — order belongs to another user")
        void getMyOrder_wrongOwner() throws Exception {
            authenticateCustomer();
            when(checkoutService.getMyOrder(any(), eq(10L)))
                    .thenThrow(OrderAccessDeniedException.forOrder(10L));

            mockMvc.perform(get("/user/order/get/10"))
                    .andExpect(status().isForbidden());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUT /user/order/cancel/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /user/order/cancel/{id}")
    class CancelOrderTests {

        @Test
        @DisplayName("204 No Content — order cancelled successfully")
        void cancelOrder_success() throws Exception {
            authenticateCustomer();
            doNothing().when(checkoutService).cancelOrder(any(), eq(10L));

            mockMvc.perform(put("/user/order/cancel/10").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 Not Found — order does not exist")
        void cancelOrder_notFound() throws Exception {
            authenticateCustomer();
            doThrow(new OrderNotFoundException("Order '99' not found."))
                    .when(checkoutService).cancelOrder(any(), eq(99L));

            mockMvc.perform(put("/user/order/cancel/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — order belongs to another user")
        void cancelOrder_wrongOwner() throws Exception {
            authenticateCustomer();
            doThrow(OrderAccessDeniedException.forOrder(10L))
                    .when(checkoutService).cancelOrder(any(), eq(10L));

            mockMvc.perform(put("/user/order/cancel/10").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 Bad Request — order in non-cancellable state")
        void cancelOrder_notCancellable() throws Exception {
            authenticateCustomer();
            doThrow(new OrderCannotBeCancelledException(10L, OrderStatus.SHIPPED))
                    .when(checkoutService).cancelOrder(any(), eq(10L));

            mockMvc.perform(put("/user/order/cancel/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUT /user/order/return/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /user/order/return/{id}")
    class RequestReturnTests {

        @Test
        @DisplayName("204 No Content — return requested successfully")
        void requestReturn_success() throws Exception {
            authenticateCustomer();
            doNothing().when(checkoutService).requestReturn(any(), eq(10L));

            mockMvc.perform(put("/user/order/return/10").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 Not Found — order does not exist")
        void requestReturn_notFound() throws Exception {
            authenticateCustomer();
            doThrow(new OrderNotFoundException("Order '99' not found."))
                    .when(checkoutService).requestReturn(any(), eq(99L));

            mockMvc.perform(put("/user/order/return/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — order belongs to another user")
        void requestReturn_wrongOwner() throws Exception {
            authenticateCustomer();
            doThrow(OrderAccessDeniedException.forOrder(10L))
                    .when(checkoutService).requestReturn(any(), eq(10L));

            mockMvc.perform(put("/user/order/return/10").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 Bad Request — order not in DELIVERED state")
        void requestReturn_notDelivered() throws Exception {
            authenticateCustomer();
            doThrow(new OrderInvalidStatusTransitionException(OrderStatus.SHIPPED, OrderStatus.RETURN_REQUEST))
                    .when(checkoutService).requestReturn(any(), eq(10L));

            mockMvc.perform(put("/user/order/return/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUT /user/order/refund/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /user/order/refund/{id}")
    class RequestRefundTests {

        @Test
        @DisplayName("204 No Content — refund requested successfully")
        void requestRefund_success() throws Exception {
            authenticateCustomer();
            doNothing().when(checkoutService).requestRefund(any(), eq(10L));

            mockMvc.perform(put("/user/order/refund/10").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 Not Found — order does not exist")
        void requestRefund_notFound() throws Exception {
            authenticateCustomer();
            doThrow(new OrderNotFoundException("Order '99' not found."))
                    .when(checkoutService).requestRefund(any(), eq(99L));

            mockMvc.perform(put("/user/order/refund/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — order belongs to another user")
        void requestRefund_wrongOwner() throws Exception {
            authenticateCustomer();
            doThrow(OrderAccessDeniedException.forOrder(10L))
                    .when(checkoutService).requestRefund(any(), eq(10L));

            mockMvc.perform(put("/user/order/refund/10").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 Bad Request — order not in DELIVERED state")
        void requestRefund_notDelivered() throws Exception {
            authenticateCustomer();
            doThrow(new OrderInvalidStatusTransitionException(OrderStatus.PLACED, OrderStatus.REFUND_REQUEST))
                    .when(checkoutService).requestRefund(any(), eq(10L));

            mockMvc.perform(put("/user/order/refund/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUT /user/order/replace/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /user/order/replace/{id}")
    class RequestReplacementTests {

        @Test
        @DisplayName("204 No Content — replacement requested successfully")
        void requestReplacement_success() throws Exception {
            authenticateCustomer();
            doNothing().when(checkoutService).requestReplacement(any(), eq(10L));

            mockMvc.perform(put("/user/order/replace/10").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 Not Found — order does not exist")
        void requestReplacement_notFound() throws Exception {
            authenticateCustomer();
            doThrow(new OrderNotFoundException("Order '99' not found."))
                    .when(checkoutService).requestReplacement(any(), eq(99L));

            mockMvc.perform(put("/user/order/replace/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — order belongs to another user")
        void requestReplacement_wrongOwner() throws Exception {
            authenticateCustomer();
            doThrow(OrderAccessDeniedException.forOrder(10L))
                    .when(checkoutService).requestReplacement(any(), eq(10L));

            mockMvc.perform(put("/user/order/replace/10").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 Bad Request — order not in DELIVERED state")
        void requestReplacement_notDelivered() throws Exception {
            authenticateCustomer();
            doThrow(new OrderInvalidStatusTransitionException(OrderStatus.CANCELLED, OrderStatus.REPLACE_REQUEST))
                    .when(checkoutService).requestReplacement(any(), eq(10L));

            mockMvc.perform(put("/user/order/replace/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Unauthenticated access to all endpoints
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Unauthenticated access")
    class UnauthenticatedTests {

        @Test
        @DisplayName("401 Unauthorized — GET /user/order/get/all")
        void getMyOrders_unauthenticated() throws Exception {
            mockMvc.perform(get("/user/order/get/all?page=0&size=20"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 Unauthorized — GET /user/order/get/{id}")
        void getMyOrder_unauthenticated() throws Exception {
            mockMvc.perform(get("/user/order/get/10"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 Unauthorized — PUT /user/order/cancel/{id}")
        void cancelOrder_unauthenticated() throws Exception {
            mockMvc.perform(put("/user/order/cancel/10").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 Unauthorized — PUT /user/order/return/{id}")
        void requestReturn_unauthenticated() throws Exception {
            mockMvc.perform(put("/user/order/return/10").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 Unauthorized — PUT /user/order/refund/{id}")
        void requestRefund_unauthenticated() throws Exception {
            mockMvc.perform(put("/user/order/refund/10").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 Unauthorized — PUT /user/order/replace/{id}")
        void requestReplacement_unauthenticated() throws Exception {
            mockMvc.perform(put("/user/order/replace/10").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Cancel endpoint — additional error scenarios
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /user/order/cancel/{id} — additional scenarios")
    class CancelOrderAdditionalTests {

        @Test
        @DisplayName("400 Bad Request — order already in CANCEL_REQUEST state")
        void cancelOrder_alreadyCancelRequest() throws Exception {
            authenticateCustomer();
            doThrow(new OrderCannotBeCancelledException(10L, OrderStatus.CANCEL_REQUEST))
                    .when(checkoutService).cancelOrder(any(), eq(10L));

            mockMvc.perform(put("/user/order/cancel/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — order in RETURN_REQUEST state")
        void cancelOrder_returnRequest() throws Exception {
            authenticateCustomer();
            doThrow(new OrderCannotBeCancelledException(10L, OrderStatus.RETURN_REQUEST))
                    .when(checkoutService).cancelOrder(any(), eq(10L));

            mockMvc.perform(put("/user/order/cancel/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Return endpoint — additional error scenarios
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /user/order/return/{id} — additional scenarios")
    class RequestReturnAdditionalTests {

        @Test
        @DisplayName("400 Bad Request — order already in RETURN_REQUEST state")
        void requestReturn_alreadyReturnRequest() throws Exception {
            authenticateCustomer();
            doThrow(new OrderInvalidStatusTransitionException(OrderStatus.RETURN_REQUEST, OrderStatus.RETURN_REQUEST))
                    .when(checkoutService).requestReturn(any(), eq(10L));

            mockMvc.perform(put("/user/order/return/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — order already RETURNED")
        void requestReturn_alreadyReturned() throws Exception {
            authenticateCustomer();
            doThrow(new OrderInvalidStatusTransitionException(OrderStatus.RETURNED, OrderStatus.RETURN_REQUEST))
                    .when(checkoutService).requestReturn(any(), eq(10L));

            mockMvc.perform(put("/user/order/return/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Refund endpoint — additional error scenarios
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /user/order/refund/{id} — additional scenarios")
    class RequestRefundAdditionalTests {

        @Test
        @DisplayName("400 Bad Request — order already in REFUND_REQUEST state")
        void requestRefund_alreadyRefundRequest() throws Exception {
            authenticateCustomer();
            doThrow(new OrderInvalidStatusTransitionException(OrderStatus.REFUND_REQUEST, OrderStatus.REFUND_REQUEST))
                    .when(checkoutService).requestRefund(any(), eq(10L));

            mockMvc.perform(put("/user/order/refund/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — order already REFUNDED")
        void requestRefund_alreadyRefunded() throws Exception {
            authenticateCustomer();
            doThrow(new OrderInvalidStatusTransitionException(OrderStatus.REFUNDED, OrderStatus.REFUND_REQUEST))
                    .when(checkoutService).requestRefund(any(), eq(10L));

            mockMvc.perform(put("/user/order/refund/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Replace endpoint — additional error scenarios
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /user/order/replace/{id} — additional scenarios")
    class RequestReplacementAdditionalTests {

        @Test
        @DisplayName("400 Bad Request — order already in REPLACE_REQUEST state")
        void requestReplacement_alreadyReplaceRequest() throws Exception {
            authenticateCustomer();
            doThrow(new OrderInvalidStatusTransitionException(OrderStatus.REPLACE_REQUEST, OrderStatus.REPLACE_REQUEST))
                    .when(checkoutService).requestReplacement(any(), eq(10L));

            mockMvc.perform(put("/user/order/replace/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — order already REPLACED")
        void requestReplacement_alreadyReplaced() throws Exception {
            authenticateCustomer();
            doThrow(new OrderInvalidStatusTransitionException(OrderStatus.REPLACED, OrderStatus.REPLACE_REQUEST))
                    .when(checkoutService).requestReplacement(any(), eq(10L));

            mockMvc.perform(put("/user/order/replace/10").with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }
}
