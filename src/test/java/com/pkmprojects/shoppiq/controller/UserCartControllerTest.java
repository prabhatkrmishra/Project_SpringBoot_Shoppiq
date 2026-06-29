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
import com.pkmprojects.shoppiq.dto.request.AddCartItemRequest;
import com.pkmprojects.shoppiq.dto.response.CartItemResponse;
import com.pkmprojects.shoppiq.dto.response.CartResponse;
import com.pkmprojects.shoppiq.dto.request.UpdateCartItemRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.CartItemAccessDeniedException;
import com.pkmprojects.shoppiq.exception.CartItemNotFoundException;
import com.pkmprojects.shoppiq.exception.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.ItemDetailsNotFoundException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.CartService;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-slice tests for {@link UserCartController}.
 *
 * <p>
 * Uses {@code @WebMvcTest} to load only the web layer; {@link CartService}
 * is mocked. The real {@link SecurityConfig} and JWT infrastructure are
 * imported so the security filter chain functions correctly.
 * </p>
 *
 * <p>
 * Tests set the {@link org.springframework.security.core.context.SecurityContext}
 * with a {@link UsernamePasswordAuthenticationToken} that carries the
 * application {@link User} entity as principal, matching how
 * {@link JwtAuthenticationFilter} configures authentication at runtime.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@WebMvcTest(UserCartController.class)
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
@DisplayName("UserCartController Tests")
class UserCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    // ---------------------------------------------------------------
    // Fixture helpers
    // ---------------------------------------------------------------

    private User authenticatedUser;

    private static CartItemResponse stubCartItemResponse(int qty) {
        BigDecimal unit = new BigDecimal("90.00");
        BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(qty));
        return new CartItemResponse(
                100L, 10L,
                "Test Product", "BrandX", "SKU-001",
                unit, new BigDecimal("100.00"), new BigDecimal("10.00"),
                qty, lineTotal
        );
    }

    private static CartResponse stubCartResponse(List<CartItemResponse> items) {
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(1L, items.size(), subtotal, items);
    }

    @BeforeEach
    void setUp() {
        authenticatedUser = User.builder()
                .name("Alice")
                .username("alice")
                .email("alice@example.com")
                .password("hashed")
                .enabled(true)
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ---------------------------------------------------------------
    // POST /user/cart/create
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("POST /user/cart/create")
    class CreateCartItem {

        @Test
        @DisplayName("Returns 201 Created with cart item body on success")
        void create_validRequest_returns201() throws Exception {
            AddCartItemRequest request = new AddCartItemRequest(10L, 2);
            CartItemResponse response = stubCartItemResponse(2);

            when(cartService.create(any(User.class), any(AddCartItemRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/user/cart/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cartItemId").value(100))
                    .andExpect(jsonPath("$.sku").value("SKU-001"))
                    .andExpect(jsonPath("$.quantity").value(2))
                    .andExpect(jsonPath("$.unitPrice").value(90.00))
                    .andExpect(jsonPath("$.lineTotal").value(180.00));
        }

        @Test
        @DisplayName("Returns 400 when itemDetailsId is null")
        void create_nullItemId_returns400() throws Exception {
            String body = "{\"itemDetailsId\": null, \"quantity\": 1}";

            mockMvc.perform(post("/user/cart/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(cartService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Returns 400 when quantity is zero")
        void create_zeroQuantity_returns400() throws Exception {
            String body = "{\"itemDetailsId\": 10, \"quantity\": 0}";

            mockMvc.perform(post("/user/cart/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(cartService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Returns 400 when quantity is negative")
        void create_negativeQuantity_returns400() throws Exception {
            String body = "{\"itemDetailsId\": 10, \"quantity\": -5}";

            mockMvc.perform(post("/user/cart/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verify(cartService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Returns 400 when requested quantity exceeds stock")
        void create_insufficientStock_returns400() throws Exception {
            AddCartItemRequest request = new AddCartItemRequest(10L, 999);

            when(cartService.create(any(User.class), any(AddCartItemRequest.class)))
                    .thenThrow(InsufficientStockException.forItem("SKU-001", 999, 50));

            mockMvc.perform(post("/user/cart/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("CART-400-001"));
        }

        @Test
        @DisplayName("Returns 404 when item details are not found")
        void create_itemDetailsNotFound_returns404() throws Exception {
            AddCartItemRequest request = new AddCartItemRequest(999L, 1);

            when(cartService.create(any(User.class), any(AddCartItemRequest.class)))
                    .thenThrow(new ItemDetailsNotFoundException(
                            "Item details with id '999' were not found."));

            mockMvc.perform(post("/user/cart/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM_DETAILS-404-002"));
        }

        @Test
        @DisplayName("Returns 401 when request is unauthenticated")
        void create_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();
            AddCartItemRequest request = new AddCartItemRequest(10L, 1);

            mockMvc.perform(post("/user/cart/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------------------------------------------------
    // GET /user/cart/get
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("GET /user/cart/get")
    class GetCart {

        @Test
        @DisplayName("Returns 200 with full cart when user has items")
        void get_withItems_returns200() throws Exception {
            List<CartItemResponse> items = List.of(
                    stubCartItemResponse(2),
                    stubCartItemResponse(3)
            );
            CartResponse cartResponse = stubCartResponse(items);

            when(cartService.get(any(User.class))).thenReturn(cartResponse);

            mockMvc.perform(get("/user/cart/get"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartId").value(1))
                    .andExpect(jsonPath("$.totalItems").value(2))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(2))
                    .andExpect(jsonPath("$.subtotal").value(450.00));
        }

        @Test
        @DisplayName("Returns 200 with empty cart when user has no items")
        void get_emptyCart_returns200() throws Exception {
            CartResponse empty = new CartResponse(null, 0, BigDecimal.ZERO, List.of());

            when(cartService.get(any(User.class))).thenReturn(empty);

            mockMvc.perform(get("/user/cart/get"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(0))
                    .andExpect(jsonPath("$.items").isEmpty())
                    .andExpect(jsonPath("$.subtotal").value(0));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void get_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/user/cart/get"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------------------------------------------------
    // GET /user/cart/get/{id}
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("GET /user/cart/get/{id}")
    class GetCartItemById {

        @Test
        @DisplayName("Returns 200 with item body when found and owned")
        void getById_found_returns200() throws Exception {
            CartItemResponse response = stubCartItemResponse(4);

            when(cartService.getById(any(User.class), eq(100L))).thenReturn(response);

            mockMvc.perform(get("/user/cart/get/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItemId").value(100))
                    .andExpect(jsonPath("$.quantity").value(4));
        }

        @Test
        @DisplayName("Returns 404 when cart item does not exist")
        void getById_notFound_returns404() throws Exception {
            when(cartService.getById(any(User.class), eq(999L)))
                    .thenThrow(CartItemNotFoundException.id(999L));

            mockMvc.perform(get("/user/cart/get/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("CART-404-001"));
        }

        @Test
        @DisplayName("Returns 403 when cart item belongs to another user")
        void getById_wrongOwner_returns403() throws Exception {
            when(cartService.getById(any(User.class), eq(200L)))
                    .thenThrow(CartItemAccessDeniedException.forItem(200L));

            mockMvc.perform(get("/user/cart/get/200"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("CART-403-001"));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void getById_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/user/cart/get/100"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------------------------------------------------
    // PUT /user/cart/update/{id}
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("PUT /user/cart/update/{id}")
    class UpdateCartItem {

        @Test
        @DisplayName("Returns 200 with updated item body on success")
        void update_valid_returns200() throws Exception {
            UpdateCartItemRequest request = new UpdateCartItemRequest(5);
            CartItemResponse response = stubCartItemResponse(5);

            when(cartService.update(any(User.class), eq(100L), any(UpdateCartItemRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/user/cart/update/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(5))
                    .andExpect(jsonPath("$.lineTotal").value(450.00));
        }

        @Test
        @DisplayName("Returns 400 when quantity is zero")
        void update_zeroQuantity_returns400() throws Exception {
            String body = "{\"quantity\": 0}";

            mockMvc.perform(put("/user/cart/update/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(cartService, never()).update(any(), any(), any());
        }

        @Test
        @DisplayName("Returns 400 when new quantity exceeds stock")
        void update_exceedsStock_returns400() throws Exception {
            UpdateCartItemRequest request = new UpdateCartItemRequest(999);

            when(cartService.update(any(User.class), eq(100L), any(UpdateCartItemRequest.class)))
                    .thenThrow(InsufficientStockException.forItem("SKU-001", 999, 50));

            mockMvc.perform(put("/user/cart/update/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("CART-400-001"));
        }

        @Test
        @DisplayName("Returns 404 when cart item does not exist")
        void update_notFound_returns404() throws Exception {
            UpdateCartItemRequest request = new UpdateCartItemRequest(2);

            when(cartService.update(any(User.class), eq(999L), any(UpdateCartItemRequest.class)))
                    .thenThrow(CartItemNotFoundException.id(999L));

            mockMvc.perform(put("/user/cart/update/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("CART-404-001"));
        }

        @Test
        @DisplayName("Returns 403 when cart item belongs to another user")
        void update_wrongOwner_returns403() throws Exception {
            UpdateCartItemRequest request = new UpdateCartItemRequest(2);

            when(cartService.update(any(User.class), eq(200L), any(UpdateCartItemRequest.class)))
                    .thenThrow(CartItemAccessDeniedException.forItem(200L));

            mockMvc.perform(put("/user/cart/update/200")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("CART-403-001"));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void update_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();
            UpdateCartItemRequest request = new UpdateCartItemRequest(2);

            mockMvc.perform(put("/user/cart/update/100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------------------------------------------------
    // DELETE /user/cart/delete/{id}
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /user/cart/delete/{id}")
    class DeleteCartItem {

        @Test
        @DisplayName("Returns 204 No Content on successful deletion")
        void delete_owned_returns204() throws Exception {
            doNothing().when(cartService).delete(any(User.class), eq(100L));

            mockMvc.perform(delete("/user/cart/delete/100"))
                    .andExpect(status().isNoContent());

            verify(cartService).delete(any(User.class), eq(100L));
        }

        @Test
        @DisplayName("Returns 404 when cart item does not exist")
        void delete_notFound_returns404() throws Exception {
            doThrow(CartItemNotFoundException.id(999L))
                    .when(cartService).delete(any(User.class), eq(999L));

            mockMvc.perform(delete("/user/cart/delete/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("CART-404-001"));
        }

        @Test
        @DisplayName("Returns 403 when cart item belongs to another user")
        void delete_wrongOwner_returns403() throws Exception {
            doThrow(CartItemAccessDeniedException.forItem(200L))
                    .when(cartService).delete(any(User.class), eq(200L));

            mockMvc.perform(delete("/user/cart/delete/200"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("CART-403-001"));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void delete_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(delete("/user/cart/delete/100"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
