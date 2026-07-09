package com.pkmprojects.shoppiq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.config.JacksonConfig;
import com.pkmprojects.shoppiq.config.SecurityConfig;
import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.order.CheckoutRequest;
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
        ProblemDetailResponseWriter.class
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

    @BeforeEach
    void setupSecurityContext() throws Exception {
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

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
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
            CheckoutRequest request = new CheckoutRequest(1L, PaymentMethod.COD, null);
            when(checkoutService.checkout(eq(customer), any(CheckoutRequest.class)))
                    .thenReturn(checkoutResponse(25L));

            mockMvc.perform(post("/user/order/checkout")
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
            CheckoutRequest request = new CheckoutRequest(1L, PaymentMethod.COD, null);
            when(checkoutService.checkout(any(), any())).thenThrow(new CartEmptyException());

            mockMvc.perform(post("/user/order/checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404 Not Found — invalid address")
        void checkout_invalidAddress() throws Exception {
            CheckoutRequest request = new CheckoutRequest(99L, PaymentMethod.COD, null);
            when(checkoutService.checkout(any(), any()))
                    .thenThrow(AddressNotFoundException.id(99L));

            mockMvc.perform(post("/user/order/checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — address belongs to different user")
        void checkout_addressWrongOwner() throws Exception {
            CheckoutRequest request = new CheckoutRequest(5L, PaymentMethod.COD, null);
            when(checkoutService.checkout(any(), any()))
                    .thenThrow(AddressAccessDeniedException.forAddress(5L));

            mockMvc.perform(post("/user/order/checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 Bad Request — insufficient stock")
        void checkout_insufficientStock() throws Exception {
            CheckoutRequest request = new CheckoutRequest(1L, PaymentMethod.COD, null);
            when(checkoutService.checkout(any(), any()))
                    .thenThrow(InsufficientStockException.forItem("SKU-1", 3, 1));

            mockMvc.perform(post("/user/order/checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request — missing addressId")
        void checkout_validation_missingAddressId() throws Exception {
            mockMvc.perform(post("/user/order/checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"paymentMethod\":\"COD\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("401 Unauthorized — unauthenticated request")
        void checkout_unauthenticated() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(post("/user/order/checkout")
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
            when(checkoutService.getMyOrders(customer))
                    .thenReturn(List.of(orderResponse(1L), orderResponse(2L)));

            mockMvc.perform(get("/user/order/get/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));
        }

        @Test
        @DisplayName("200 OK — empty list when no orders")
        void getMyOrders_empty() throws Exception {
            when(checkoutService.getMyOrders(customer)).thenReturn(List.of());

            mockMvc.perform(get("/user/order/get/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
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
            when(checkoutService.getMyOrder(customer, 10L)).thenReturn(orderResponse(10L));

            mockMvc.perform(get("/user/order/get/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.status").value("PLACED"));
        }

        @Test
        @DisplayName("404 Not Found — order does not exist")
        void getMyOrder_notFound() throws Exception {
            when(checkoutService.getMyOrder(any(), eq(99L)))
                    .thenThrow(new OrderNotFoundException("Order '99' not found."));

            mockMvc.perform(get("/user/order/get/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — order belongs to another user")
        void getMyOrder_wrongOwner() throws Exception {
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
            doNothing().when(checkoutService).cancelOrder(any(), eq(10L));

            mockMvc.perform(put("/user/order/cancel/10"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 Not Found — order does not exist")
        void cancelOrder_notFound() throws Exception {
            doThrow(new OrderNotFoundException("Order '99' not found."))
                    .when(checkoutService).cancelOrder(any(), eq(99L));

            mockMvc.perform(put("/user/order/cancel/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — order belongs to another user")
        void cancelOrder_wrongOwner() throws Exception {
            doThrow(OrderAccessDeniedException.forOrder(10L))
                    .when(checkoutService).cancelOrder(any(), eq(10L));

            mockMvc.perform(put("/user/order/cancel/10"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 Bad Request — order in non-cancellable state")
        void cancelOrder_notCancellable() throws Exception {
            doThrow(new OrderCannotBeCancelledException(10L, OrderStatus.SHIPPED))
                    .when(checkoutService).cancelOrder(any(), eq(10L));

            mockMvc.perform(put("/user/order/cancel/10"))
                    .andExpect(status().isBadRequest());
        }
    }
}
