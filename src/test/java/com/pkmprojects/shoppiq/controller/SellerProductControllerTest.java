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
import com.pkmprojects.shoppiq.controller.seller.SellerProductController;
import com.pkmprojects.shoppiq.dto.request.ItemRequest;
import com.pkmprojects.shoppiq.dto.response.CategoryResponse;
import com.pkmprojects.shoppiq.dto.response.ItemResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.exception.DuplicateItemException;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.SellerSuspendedException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.seller.SellerProductService;
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
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SellerProductController.class)
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
@DisplayName("SellerProductController Tests")
class SellerProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SellerProductService sellerProductService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private static final Long ITEM_ID = 1L;
    private static final String ITEM_NAME = "Test Product";
    private static final String ITEM_DESC = "A test product description";
    private static final String BRAND = "TestBrand";
    private static final String SKU = "TST-001";
    private static final BigDecimal PRICE = new BigDecimal("99.99");
    private static final int STOCK = 10;
    private static final BigDecimal DISCOUNT = BigDecimal.ZERO;
    private static final Long CATEGORY_ID = 1L;

    private static ItemResponse stubResponse(Long id) {
        return new ItemResponse(
                ProductPublishingStatus.DRAFT,
                id, ITEM_NAME, ITEM_DESC, BRAND, SKU, PRICE, STOCK, DISCOUNT,
                new CategoryResponse(CATEGORY_ID, "Electronics", "electronics", "Gadgets"),
                Instant.now(), Instant.now()
        );
    }

    private ItemRequest validRequest;
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

        validRequest = new ItemRequest(
                ITEM_NAME, ITEM_DESC, BRAND, SKU, PRICE, STOCK, DISCOUNT, CATEGORY_ID
        );
    }

    @Nested
    @DisplayName("POST /seller/products")
    class CreateProduct {

        @Test
        @DisplayName("Returns 201 Created with response body on success")
        void create_validRequest_returns201() throws Exception {
            when(sellerProductService.createProduct(any(ItemRequest.class), any(User.class)))
                    .thenReturn(stubResponse(ITEM_ID));

            mockMvc.perform(post("/seller/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ITEM_ID))
                    .andExpect(jsonPath("$.name").value(ITEM_NAME))
                    .andExpect(jsonPath("$.publishingStatus").value("DRAFT"));
        }

        @Test
        @DisplayName("Returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            ItemRequest invalid = new ItemRequest(
                    "", ITEM_DESC, BRAND, SKU, PRICE, STOCK, DISCOUNT, CATEGORY_ID
            );

            mockMvc.perform(post("/seller/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(sellerProductService, never()).createProduct(any(), any());
        }

        @Test
        @DisplayName("Returns 409 Conflict when SKU already exists")
        void create_duplicateSku_returns409() throws Exception {
            when(sellerProductService.createProduct(any(ItemRequest.class), any(User.class)))
                    .thenThrow(DuplicateItemException.sku(SKU));

            mockMvc.perform(post("/seller/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-409-001"));
        }

        @Test
        @DisplayName("Returns 404 when seller profile does not exist")
        void create_sellerNotFound_returns404() throws Exception {
            when(sellerProductService.createProduct(any(ItemRequest.class), any(User.class)))
                    .thenThrow(SellerNotFoundException.userId(1L));

            mockMvc.perform(post("/seller/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-404-001"));
        }

        @Test
        @DisplayName("Returns 400 when seller is not verified")
        void create_sellerNotVerified_returns400() throws Exception {
            when(sellerProductService.createProduct(any(ItemRequest.class), any(User.class)))
                    .thenThrow(SellerNotVerifiedException.forAction(1L, "manage products"));

            mockMvc.perform(post("/seller/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-400-001"));
        }

        @Test
        @DisplayName("Returns 400 when seller is suspended")
        void create_sellerSuspended_returns400() throws Exception {
            when(sellerProductService.createProduct(any(ItemRequest.class), any(User.class)))
                    .thenThrow(SellerSuspendedException.forAction(1L, "manage products"));

            mockMvc.perform(post("/seller/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-400-002"));
        }
    }

    @Nested
    @DisplayName("GET /seller/products")
    class GetMyProducts {

        @Test
        @DisplayName("Returns 200 with list of products")
        void getMyProducts_returnsList() throws Exception {
            when(sellerProductService.getMyProducts(any(User.class)))
                    .thenReturn(List.of(stubResponse(1L), stubResponse(2L)));

            mockMvc.perform(get("/seller/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));
        }

        @Test
        @DisplayName("Returns 200 with empty list when seller has no products")
        void getMyProducts_emptyList() throws Exception {
            when(sellerProductService.getMyProducts(any(User.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/seller/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /seller/products/{id}")
    class GetMyProductById {

        @Test
        @DisplayName("Returns 200 with product when found")
        void getMyProductById_found_returns200() throws Exception {
            when(sellerProductService.getMyProductById(eq(1L), any(User.class)))
                    .thenReturn(stubResponse(1L));

            mockMvc.perform(get("/seller/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value(ITEM_NAME));
        }

        @Test
        @DisplayName("Returns 404 when product does not belong to seller")
        void getMyProductById_notFound_returns404() throws Exception {
            when(sellerProductService.getMyProductById(eq(99L), any(User.class)))
                    .thenThrow(ItemNotFoundException.id(99L));

            mockMvc.perform(get("/seller/products/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-404-001"));
        }
    }

    @Nested
    @DisplayName("PUT /seller/products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("Returns 200 with updated product on success")
        void update_validRequest_returns200() throws Exception {
            when(sellerProductService.updateProduct(eq(1L), any(ItemRequest.class), any(User.class)))
                    .thenReturn(stubResponse(1L));

            mockMvc.perform(put("/seller/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value(ITEM_NAME));
        }

        @Test
        @DisplayName("Returns 404 when product does not belong to seller")
        void update_productNotFound_returns404() throws Exception {
            when(sellerProductService.updateProduct(eq(99L), any(ItemRequest.class), any(User.class)))
                    .thenThrow(ItemNotFoundException.id(99L));

            mockMvc.perform(put("/seller/products/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-404-001"));
        }

        @Test
        @DisplayName("Returns 409 when updated SKU conflicts with another product")
        void update_duplicateSku_returns409() throws Exception {
            when(sellerProductService.updateProduct(eq(1L), any(ItemRequest.class), any(User.class)))
                    .thenThrow(DuplicateItemException.sku("TST-002"));

            mockMvc.perform(put("/seller/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-409-001"));
        }
    }

    @Nested
    @DisplayName("DELETE /seller/products/{id}")
    class DeleteProduct {

        @Test
        @DisplayName("Returns 200 on successful deletion")
        void delete_existingProduct_returns200() throws Exception {
            doNothing().when(sellerProductService).deleteProduct(eq(1L), any(User.class));

            mockMvc.perform(delete("/seller/products/1"))
                    .andExpect(status().isOk());

            verify(sellerProductService).deleteProduct(eq(1L), any(User.class));
        }

        @Test
        @DisplayName("Returns 404 when product does not belong to seller")
        void delete_productNotFound_returns404() throws Exception {
            doThrow(ItemNotFoundException.id(99L))
                    .when(sellerProductService).deleteProduct(eq(99L), any(User.class));

            mockMvc.perform(delete("/seller/products/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-404-001"));
        }
    }
}
