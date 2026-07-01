package com.pkmprojects.shoppiq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.config.*;
import com.pkmprojects.shoppiq.controller.admin.AdminController;
import com.pkmprojects.shoppiq.dto.admin.analytics.*;
import com.pkmprojects.shoppiq.dto.admin.request.*;
import com.pkmprojects.shoppiq.dto.admin.response.*;
import com.pkmprojects.shoppiq.enums.*;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.*;
import com.pkmprojects.shoppiq.service.admin.*;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
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
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminDashboardService dashboardService;

    @MockitoBean
    private AdminInventoryService inventoryService;

    @MockitoBean
    private AdminOrderService orderService;

    @MockitoBean
    private AdminUserService userService;

    @MockitoBean
    private AdminPaymentService paymentService;

    @MockitoBean
    private AdminReviewService reviewService;

    @MockitoBean
    private AdminReportService reportService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private DashboardSummaryResponse dashboardSummary;
    private SalesAnalyticsResponse salesAnalytics;
    private RecentActivityResponse recentActivity;

    @BeforeEach
    void setUp() {
        dashboardSummary = DashboardSummaryResponse.from(
                100L, 50L, 200L, 5L,
                new BigDecimal("25000.00"), 3L, 2L, 1L, 4L
        );

        salesAnalytics = new SalesAnalyticsResponse(
                List.of(), List.of(), List.of(), List.of(), List.of(), Map.of()
        );

        recentActivity = new RecentActivityResponse(
                List.of(), List.of(), List.of(), List.of()
        );
    }

    @Nested
    @DisplayName("Dashboard Endpoints")
    class DashboardTests {

        @Test
        @DisplayName("GET /admin/dashboard/summary - returns summary")
        @WithMockUser(roles = "ADMIN")
        void getDashboardSummary_returnsSummary() throws Exception {
            when(dashboardService.getDashboardSummary()).thenReturn(dashboardSummary);

            mockMvc.perform(get("/api/admin/dashboard/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalUsers").value(100))
                    .andExpect(jsonPath("$.totalProducts").value(50))
                    .andExpect(jsonPath("$.totalOrders").value(200));
        }

        @Test
        @DisplayName("GET /admin/dashboard/summary - requires ADMIN role")
        void getDashboardSummary_requiresAdminRole() throws Exception {
            mockMvc.perform(get("/api/admin/dashboard/summary"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /admin/dashboard/sales-analytics - returns analytics")
        @WithMockUser(roles = "ADMIN")
        void getSalesAnalytics_returnsAnalytics() throws Exception {
            when(dashboardService.getSalesAnalytics()).thenReturn(salesAnalytics);

            mockMvc.perform(get("/api/admin/dashboard/sales-analytics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dailySales").isArray())
                    .andExpect(jsonPath("$.topSellingProducts").isArray());
        }

        @Test
        @DisplayName("GET /admin/dashboard/recent-activity - returns activity")
        @WithMockUser(roles = "ADMIN")
        void getRecentActivity_returnsActivity() throws Exception {
            when(dashboardService.getRecentActivity()).thenReturn(recentActivity);

            mockMvc.perform(get("/api/admin/dashboard/recent-activity"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recentOrders").isArray())
                    .andExpect(jsonPath("$.recentPayments").isArray());
        }
    }

    @Nested
    @DisplayName("Inventory Endpoints")
    class InventoryTests {

        @Test
        @DisplayName("GET /admin/inventory - returns inventory list")
        @WithMockUser(roles = "ADMIN")
        void getAllInventory_returnsList() throws Exception {
            when(inventoryService.getAllProductInventory()).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /admin/inventory/low-stock - returns low stock products")
        @WithMockUser(roles = "ADMIN")
        void getLowStockProducts_returnsList() throws Exception {
            when(inventoryService.getLowStockProducts()).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/inventory/low-stock"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /admin/inventory/{itemId} - adjusts stock")
        @WithMockUser(roles = "ADMIN")
        void adjustStock_returnsUpdated() throws Exception {
            StockAdjustmentRequest request = new StockAdjustmentRequest(10, "New shipment");
            AdminProductInventoryResponse response = AdminProductInventoryResponse.from(
                    1L, "Product A", "Desc", "Category", "SKU-001", "Brand",
                    new BigDecimal("99.99"), BigDecimal.ZERO,
                    60, 5, true
            );
            when(inventoryService.adjustStock(eq(1L), any())).thenReturn(response);

            mockMvc.perform(put("/api/admin/inventory/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemName").value("Product A"));
        }

        @Test
        @DisplayName("GET /admin/inventory/summary - returns summary")
        @WithMockUser(roles = "ADMIN")
        void getInventorySummary_returnsSummary() throws Exception {
            AdminInventoryService.InventoryDashboardSummary summary =
                    new AdminInventoryService.InventoryDashboardSummary(50, 1000, 5, 2);
            when(inventoryService.getInventoryDashboardSummary()).thenReturn(summary);

            mockMvc.perform(get("/api/admin/inventory/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalProducts").value(50));
        }
    }

    @Nested
    @DisplayName("Order Endpoints")
    class OrderTests {

        @Test
        @DisplayName("GET /admin/orders - returns paginated orders")
        @WithMockUser(roles = "ADMIN")
        void getAllOrders_returnsPage() throws Exception {
            AdminOrderService.PageResponse<AdminOrderResponse> pageResponse =
                    new AdminOrderService.PageResponse<>(List.of(), 0, 20, 0, 0, true, true);
            when(orderService.getAllOrders(isNull(), eq(0), eq(20))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/admin/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("PUT /admin/orders/{id}/status - updates status")
        @WithMockUser(roles = "ADMIN")
        void updateOrderStatus_returnsUpdated() throws Exception {
            AdminOrderResponse response = mock(AdminOrderResponse.class);
            when(orderService.updateOrderStatus(eq(1L), eq(OrderStatus.CONFIRMED))).thenReturn(response);

            mockMvc.perform(put("/api/admin/orders/1/status")
                            .param("status", "CONFIRMED"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("User Endpoints")
    class UserTests {

        @Test
        @DisplayName("GET /admin/users - returns paginated users")
        @WithMockUser(roles = "ADMIN")
        void getAllCustomers_returnsPage() throws Exception {
            AdminUserService.PageResponse<AdminUserResponse> pageResponse =
                    new AdminUserService.PageResponse<>(List.of(), 0, 20, 0, 0, true, true);
            when(userService.getAllCustomers(isNull(), eq(0), eq(20))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /admin/users/{id}/block - blocks user")
        @WithMockUser(roles = "ADMIN")
        void blockCustomer_returnsUpdated() throws Exception {
            AdminUserResponse response = mock(AdminUserResponse.class);
            when(userService.blockCustomer(eq(1L))).thenReturn(response);

            mockMvc.perform(put("/api/admin/users/1/block"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /admin/users/{id}/unblock - unblocks user")
        @WithMockUser(roles = "ADMIN")
        void unblockCustomer_returnsUpdated() throws Exception {
            AdminUserResponse response = mock(AdminUserResponse.class);
            when(userService.unblockCustomer(eq(1L))).thenReturn(response);

            mockMvc.perform(put("/api/admin/users/1/unblock"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /admin/users/stats - returns stats")
        @WithMockUser(roles = "ADMIN")
        void getCustomerStats_returnsStats() throws Exception {
            AdminUserService.CustomerDashboardStats stats =
                    new AdminUserService.CustomerDashboardStats(100, 80, 20, 10,
                            new BigDecimal("50000"), new BigDecimal("500"));
            when(userService.getCustomerDashboardStats()).thenReturn(stats);

            mockMvc.perform(get("/api/admin/users/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCustomers").value(100));
        }
    }

    @Nested
    @DisplayName("Payment Endpoints")
    class PaymentTests {

        @Test
        @DisplayName("GET /admin/payments - returns paginated payments")
        @WithMockUser(roles = "ADMIN")
        void getAllPayments_returnsPage() throws Exception {
            AdminPaymentService.PageResponse<AdminPaymentResponse> pageResponse =
                    new AdminPaymentService.PageResponse<>(List.of(), 0, 20, 0, 0, true, true);
            when(paymentService.getAllPayments(isNull(), eq(0), eq(20))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/admin/payments"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /admin/payments/{id}/refund - refunds payment")
        @WithMockUser(roles = "ADMIN")
        void refundPayment_returnsUpdated() throws Exception {
            AdminPaymentResponse response = mock(AdminPaymentResponse.class);
            when(paymentService.refundPayment(eq(1L))).thenReturn(response);

            mockMvc.perform(put("/api/admin/payments/1/refund"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /admin/payments/stats - returns stats")
        @WithMockUser(roles = "ADMIN")
        void getPaymentStats_returnsStats() throws Exception {
            AdminPaymentService.PaymentDashboardStats stats =
                    new AdminPaymentService.PaymentDashboardStats(200, 150, 30, 20,
                            new BigDecimal("100000"), new BigDecimal("5000"));
            when(paymentService.getPaymentDashboardStats()).thenReturn(stats);

            mockMvc.perform(get("/api/admin/payments/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPayments").value(200));
        }
    }

    @Nested
    @DisplayName("Review Endpoints")
    class ReviewTests {

        @Test
        @DisplayName("GET /admin/reviews - returns paginated reviews")
        @WithMockUser(roles = "ADMIN")
        void getAllReviews_returnsPage() throws Exception {
            AdminReviewService.PageResponse<AdminReviewResponse> pageResponse =
                    new AdminReviewService.PageResponse<>(List.of(), 0, 20, 0, 0, true, true);
            when(reviewService.getAllReviews(eq(0), eq(20))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/admin/reviews"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /admin/reviews/{id} - deletes review")
        @WithMockUser(roles = "ADMIN")
        void deleteReview_returnsNoContent() throws Exception {
            mockMvc.perform(delete("/api/admin/reviews/1"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Admin endpoints require ADMIN role")
        void adminEndpoints_requireAdminRole() throws Exception {
            mockMvc.perform(get("/api/admin/dashboard/summary"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/admin/inventory"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/admin/orders"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/admin/payments"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/admin/reviews"))
                    .andExpect(status().isUnauthorized());
        }
    }
}