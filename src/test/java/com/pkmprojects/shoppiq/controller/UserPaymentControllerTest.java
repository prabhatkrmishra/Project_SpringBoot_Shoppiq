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
import com.pkmprojects.shoppiq.dto.payment.PaymentResponse;
import com.pkmprojects.shoppiq.dto.payment.PaymentStatusResponse;
import com.pkmprojects.shoppiq.dto.payment.VerifyPaymentRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.PaymentService;
import com.pkmprojects.shoppiq.service.RolesService;
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
 * Controller slice integration tests for {@link UserPaymentController}.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@WebMvcTest(UserPaymentController.class)
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
@DisplayName("UserPaymentController Tests")
class UserPaymentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean PaymentService paymentService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean RolesService rolesService;
    @MockitoBean HttpCookieOAuth2AuthorizationRequestRepository cookieRepo;
    @MockitoBean OAuth2SuccessHandler oAuth2SuccessHandler;

    private User customer;
    private User admin;

    private static void setId(Object entity, Long id) throws Exception {
        Field f = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }

    @BeforeEach
    void setupCustomer() throws Exception {
        customer = User.builder().name("Alice").username("alice")
                .email("alice@test.com").password("hashed").enabled(true).build();
        setId(customer, 1L);
        setSecurityContext(customer, "ROLE_CUSTOMER");
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(User user, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ─── Stub builders ────────────────────────────────────────────────────

    private PaymentResponse paymentResponse(long id) {
        return new PaymentResponse(
                id, 10L, "PAY-20260701-10",
                PaymentMethod.COD, PaymentStatus.PENDING, PaymentGateway.NONE,
                BigDecimal.valueOf(500), "INR", null, null, null,
                Instant.now(), Instant.now()
        );
    }

    private PaymentStatusResponse statusResponse(PaymentStatus status) {
        return new PaymentStatusResponse(1L, "PAY-20260701-10", status, null);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GET /user/payment/get/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /user/payment/get/{id}")
    class GetPaymentTests {

        @Test
        @DisplayName("200 OK — returns payment detail")
        void getPayment_success() throws Exception {
            when(paymentService.getPayment(eq(customer), eq(1L)))
                    .thenReturn(paymentResponse(1L));

            mockMvc.perform(get("/user/payment/get/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.paymentMethod").value("COD"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("404 Not Found — payment does not exist")
        void getPayment_notFound() throws Exception {
            when(paymentService.getPayment(any(), eq(99L)))
                    .thenThrow(PaymentNotFoundException.forId(99L));

            mockMvc.perform(get("/user/payment/get/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — payment belongs to another user")
        void getPayment_wrongOwner() throws Exception {
            when(paymentService.getPayment(any(), eq(1L)))
                    .thenThrow(PaymentAccessDeniedException.forPayment(1L));

            mockMvc.perform(get("/user/payment/get/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("401 Unauthorized — unauthenticated request")
        void getPayment_unauthenticated() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/user/payment/get/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POST /user/payment/pay/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /user/payment/pay/{id}")
    class PayTests {

        @Test
        @DisplayName("200 OK — payment initiated")
        void pay_success() throws Exception {
            when(paymentService.pay(eq(customer), eq(1L)))
                    .thenReturn(statusResponse(PaymentStatus.PROCESSING));

            mockMvc.perform(post("/user/payment/pay/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PROCESSING"));
        }

        @Test
        @DisplayName("400 Bad Request — payment already paid")
        void pay_alreadyPaid() throws Exception {
            when(paymentService.pay(any(), eq(1L)))
                    .thenThrow(PaymentInvalidStateException.alreadyPaid(1L));

            mockMvc.perform(post("/user/payment/pay/1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404 Not Found — payment not found")
        void pay_notFound() throws Exception {
            when(paymentService.pay(any(), eq(99L)))
                    .thenThrow(PaymentNotFoundException.forId(99L));

            mockMvc.perform(post("/user/payment/pay/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("403 Forbidden — wrong user")
        void pay_wrongOwner() throws Exception {
            when(paymentService.pay(any(), eq(1L)))
                    .thenThrow(PaymentAccessDeniedException.forPayment(1L));

            mockMvc.perform(post("/user/payment/pay/1"))
                    .andExpect(status().isForbidden());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // POST /user/payment/verify
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /user/payment/verify")
    class VerifyPaymentTests {

        @Test
        @DisplayName("200 OK — payment verified as PAID")
        void verifyPayment_success() throws Exception {
            when(paymentService.verifyPayment(eq(customer), eq(1L), eq("TXN-001")))
                    .thenReturn(statusResponse(PaymentStatus.PAID));

            mockMvc.perform(post("/user/payment/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"paymentId\":1,\"transactionId\":\"TXN-001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAID"));
        }

        @Test
        @DisplayName("400 Bad Request — missing paymentId/transactionId")
        void verifyPayment_missingField() throws Exception {
            mockMvc.perform(post("/user/payment/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404 Not Found — unknown paymentId")
        void verifyPayment_notFound() throws Exception {
            when(paymentService.verifyPayment(any(), eq(1L), eq("BAD-ID")))
                    .thenThrow(PaymentNotFoundException.forTransactionId("BAD-ID"));

            mockMvc.perform(post("/user/payment/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"paymentId\":1,\"transactionId\":\"BAD-ID\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUT /user/payment/cancel/{id}
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /user/payment/cancel/{id}")
    class CancelPaymentTests {

        @Test
        @DisplayName("200 OK — payment cancelled")
        void cancelPayment_success() throws Exception {
            when(paymentService.cancelPayment(eq(customer), eq(1L)))
                    .thenReturn(statusResponse(PaymentStatus.CANCELLED));

            mockMvc.perform(put("/user/payment/cancel/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("400 Bad Request — payment cannot be cancelled (PAID)")
        void cancelPayment_invalidState() throws Exception {
            when(paymentService.cancelPayment(any(), eq(1L)))
                    .thenThrow(PaymentInvalidStateException.cannotCancel(1L, PaymentStatus.PAID));

            mockMvc.perform(put("/user/payment/cancel/1"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PUT /user/payment/refund/{id}  — REMOVED (admin uses own endpoint)
    // ═══════════════════════════════════════════════════════════════════════
}
