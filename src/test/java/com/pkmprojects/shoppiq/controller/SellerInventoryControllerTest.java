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
import com.pkmprojects.shoppiq.controller.seller.SellerInventoryController;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.seller.response.SellerInventoryResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.ProductPublishingStatus;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.ItemStockNegativeException;
import com.pkmprojects.shoppiq.exception.SellerNotFoundException;
import com.pkmprojects.shoppiq.exception.SellerNotVerifiedException;
import com.pkmprojects.shoppiq.exception.SellerSuspendedException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.seller.SellerInventoryService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(SellerInventoryController.class)
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
@DisplayName("SellerInventoryController Tests")
class SellerInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerInventoryService sellerInventoryService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private static SellerInventoryResponse stubResponse(Long id, int stock) {
        SellerInventoryResponse.StockStatus status;
        if (stock == 0) {
            status = SellerInventoryResponse.StockStatus.OUT_OF_STOCK;
        } else if (stock <= 5) {
            status = SellerInventoryResponse.StockStatus.LOW_STOCK;
        } else {
            status = SellerInventoryResponse.StockStatus.IN_STOCK;
        }
        return new SellerInventoryResponse(
                id, "Product " + id, "SKU-" + id, "Brand",
                BigDecimal.valueOf(99.99), stock, status, ProductPublishingStatus.DRAFT, null
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
    @DisplayName("GET /seller/inventory")
    class GetInventory {

        @Test
        @DisplayName("Returns 200 with inventory list")
        void getInventory_returnsList() throws Exception {
            when(sellerInventoryService.getInventory(any(User.class), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(stubResponse(1L, 10), stubResponse(2L, 0)), 0, 20, 2, 1, true, false));

            mockMvc.perform(get("/seller/inventory?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].itemId").value(1))
                    .andExpect(jsonPath("$.content[0].stockStatus").value("IN_STOCK"))
                    .andExpect(jsonPath("$.content[1].stockStatus").value("OUT_OF_STOCK"));
        }

        @Test
        @DisplayName("Returns 200 with empty list when seller has no products")
        void getInventory_emptyList() throws Exception {
            when(sellerInventoryService.getInventory(any(User.class), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 1, true, false));

            mockMvc.perform(get("/seller/inventory?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }

        @Test
        @DisplayName("Returns 404 when seller profile does not exist")
        void getInventory_sellerNotFound_returns404() throws Exception {
            when(sellerInventoryService.getInventory(any(User.class), anyInt(), anyInt()))
                    .thenThrow(SellerNotFoundException.userId(1L));

            mockMvc.perform(get("/seller/inventory?page=0&size=20"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-404-001"));
        }
    }

    @Nested
    @DisplayName("GET /seller/inventory/low-stock")
    class GetLowStock {

        @Test
        @DisplayName("Returns 200 with low stock products")
        void getLowStock_returnsList() throws Exception {
            when(sellerInventoryService.getLowStockProducts(any(User.class), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(stubResponse(3L, 3), stubResponse(4L, 1)), 0, 20, 2, 1, true, false));

            mockMvc.perform(get("/seller/inventory/low-stock?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].stockStatus").value("LOW_STOCK"))
                    .andExpect(jsonPath("$.content[1].stockStatus").value("LOW_STOCK"));
        }

        @Test
        @DisplayName("Returns 200 with empty list when no low stock products")
        void getLowStock_emptyList() throws Exception {
            when(sellerInventoryService.getLowStockProducts(any(User.class), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 1, true, false));

            mockMvc.perform(get("/seller/inventory/low-stock?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /seller/inventory/out-of-stock")
    class GetOutOfStock {

        @Test
        @DisplayName("Returns 200 with out of stock products")
        void getOutOfStock_returnsList() throws Exception {
            when(sellerInventoryService.getOutOfStockProducts(any(User.class), anyInt(), anyInt()))
                    .thenReturn(new PageResponse<>(List.of(stubResponse(5L, 0)), 0, 20, 1, 1, true, false));

            mockMvc.perform(get("/seller/inventory/out-of-stock?page=0&size=20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].stockStatus").value("OUT_OF_STOCK"));
        }
    }

    @Nested
    @DisplayName("PUT /seller/inventory/{id}/adjust")
    class AdjustStock {

        @Test
        @DisplayName("Returns 200 with updated inventory on success")
        void adjustStock_valid_returns200() throws Exception {
            when(sellerInventoryService.adjustStock(eq(1L), eq(5), eq("New shipment"), any(User.class)))
                    .thenReturn(stubResponse(1L, 15));

            mockMvc.perform(put("/seller/inventory/1/adjust").with(csrf())
                            .param("quantity", "5")
                            .param("reason", "New shipment"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemId").value(1))
                    .andExpect(jsonPath("$.stockQuantity").value(15))
                    .andExpect(jsonPath("$.stockStatus").value("IN_STOCK"));
        }

        @Test
        @DisplayName("Returns 400 when adjustment results in negative stock")
        void adjustStock_negativeResult_returns400() throws Exception {
            when(sellerInventoryService.adjustStock(eq(1L), eq(-50), eq("Damage write-off"), any(User.class)))
                    .thenThrow(ItemStockNegativeException.forAdjustment(10, -50));

            mockMvc.perform(put("/seller/inventory/1/adjust").with(csrf())
                            .param("quantity", "-50")
                            .param("reason", "Damage write-off"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-400-001"));
        }

        @Test
        @DisplayName("Returns 404 when product does not belong to seller")
        void adjustStock_notFound_returns404() throws Exception {
            when(sellerInventoryService.adjustStock(eq(99L), eq(5), eq("Test"), any(User.class)))
                    .thenThrow(ItemNotFoundException.id(99L));

            mockMvc.perform(put("/seller/inventory/99/adjust").with(csrf())
                            .param("quantity", "5")
                            .param("reason", "Test"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-404-001"));
        }

        @Test
        @DisplayName("Returns 400 when seller is not verified")
        void adjustStock_sellerNotVerified_returns400() throws Exception {
            when(sellerInventoryService.adjustStock(eq(1L), anyInt(), anyString(), any(User.class)))
                    .thenThrow(SellerNotVerifiedException.forAction(1L, "manage inventory"));

            mockMvc.perform(put("/seller/inventory/1/adjust").with(csrf())
                            .param("quantity", "5")
                            .param("reason", "Test"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-400-001"));
        }

        @Test
        @DisplayName("Returns 400 when seller is suspended")
        void adjustStock_sellerSuspended_returns400() throws Exception {
            when(sellerInventoryService.adjustStock(eq(1L), anyInt(), anyString(), any(User.class)))
                    .thenThrow(SellerSuspendedException.forAction(1L, "manage inventory"));

            mockMvc.perform(put("/seller/inventory/1/adjust").with(csrf())
                            .param("quantity", "5")
                            .param("reason", "Test"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("SELLER-400-002"));
        }

        @Test
        @DisplayName("Returns 400 when quantity parameter is missing")
        void adjustStock_missingQuantity_returns400() throws Exception {
            mockMvc.perform(put("/seller/inventory/1/adjust").with(csrf())
                            .param("reason", "Test"))
                    .andExpect(status().isBadRequest());

            verify(sellerInventoryService, never()).adjustStock(anyLong(), anyInt(), anyString(), any());
        }

        @Test
        @DisplayName("Returns 400 when reason parameter is missing")
        void adjustStock_missingReason_returns400() throws Exception {
            mockMvc.perform(put("/seller/inventory/1/adjust").with(csrf())
                            .param("quantity", "5"))
                    .andExpect(status().isBadRequest());

            verify(sellerInventoryService, never()).adjustStock(anyLong(), anyInt(), anyString(), any());
        }
    }
}
