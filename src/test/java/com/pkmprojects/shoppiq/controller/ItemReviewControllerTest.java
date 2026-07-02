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
import com.pkmprojects.shoppiq.dto.request.ItemReviewRequest;
import com.pkmprojects.shoppiq.dto.response.ItemReviewResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.DuplicateItemReviewException;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.ItemReviewNotFoundException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.ItemReviewService;
import com.pkmprojects.shoppiq.service.RolesService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-slice tests for {@link ItemReviewController}.
 *
 * <p>Uses {@code @WebMvcTest} to load only the web layer; the service is mocked.
 * The real {@link SecurityConfig} and JWT infrastructure are imported so the
 * security filter chain functions correctly.</p>
 *
 * <p>Tests manually set the {@code SecurityContext} with a
 * {@link UsernamePasswordAuthenticationToken} containing the application's
 * {@link User} entity as principal, matching how
 * {@link JwtAuthenticationFilter} configures authentication.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@WebMvcTest(ItemReviewController.class)
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
@DisplayName("ItemReviewController Tests")
class ItemReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemReviewService itemReviewService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    // ---------------------------------------------------------------
    // Fixture
    // ---------------------------------------------------------------

    private static ItemReviewResponse stubResponse(int rating, String review) {
        return new ItemReviewResponse(
                100L, 10L, "Test Product", 1L, "Alice", "alice",
                rating, review, Instant.now(), Instant.now()
        );
    }

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = User.builder()
                .name("Alice")
                .username("alice")
                .email("alice@example.com")
                .password("hashed")
                .enabled(true)
                .build();

        // Set the User entity as principal, matching JwtAuthenticationFilter behavior
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // ---------------------------------------------------------------
    // POST /items/{itemId}/create/review
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("POST /items/{itemId}/create/review")
    class CreateReview {

        @Test
        @DisplayName("Returns 201 Created with response body on success")
        void create_validRequest_returns201() throws Exception {
            ItemReviewRequest request = new ItemReviewRequest(5, "Amazing!");
            ItemReviewResponse response = stubResponse(5, "Amazing!");

            when(itemReviewService.create(eq(1L), any(User.class), any(ItemReviewRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/items/1/create/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.review").value("Amazing!"))
                    .andExpect(jsonPath("$.reviewerUsername").value("alice"));
        }

        @Test
        @DisplayName("Returns 409 Conflict when user has already reviewed the item")
        void create_duplicateReview_returns409() throws Exception {
            ItemReviewRequest request = new ItemReviewRequest(3, "Again");

            when(itemReviewService.create(eq(1L), any(User.class), any(ItemReviewRequest.class)))
                    .thenThrow(DuplicateItemReviewException.userId(42L));

            mockMvc.perform(post("/items/1/create/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("ITEM_REVIEW-409-001"));
        }

        @Test
        @DisplayName("Returns 404 when item does not exist")
        void create_itemNotFound_returns404() throws Exception {
            ItemReviewRequest request = new ItemReviewRequest(3, "Good");

            when(itemReviewService.create(eq(99L), any(User.class), any(ItemReviewRequest.class)))
                    .thenThrow(ItemNotFoundException.id(99L));

            mockMvc.perform(post("/items/99/create/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-404-001"));
        }

        @Test
        @DisplayName("Returns 400 when rating is below minimum")
        void create_ratingTooLow_returns400() throws Exception {
            ItemReviewRequest request = new ItemReviewRequest(0, "Bad rating");

            mockMvc.perform(post("/items/1/create/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(itemReviewService, never()).create(any(), any(), any());
        }

        @Test
        @DisplayName("Returns 400 when rating exceeds maximum")
        void create_ratingTooHigh_returns400() throws Exception {
            ItemReviewRequest request = new ItemReviewRequest(6, "Out of range");

            mockMvc.perform(post("/items/1/create/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));
        }

        @Test
        @DisplayName("Returns 400 when rating is null")
        void create_nullRating_returns400() throws Exception {
            String body = "{\"review\":\"No rating supplied\"}";

            mockMvc.perform(post("/items/1/create/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));
        }

        @Test
        @DisplayName("Returns 400 when request body is a JSON array instead of an object")
        void create_arrayBody_returns400() throws Exception {
            String arrayBody = "[{\"rating\":5,\"review\":\"test\"}]";

            mockMvc.perform(post("/items/1/create/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(arrayBody))
                    .andExpect(status().isBadRequest());
        }
    }

    // ---------------------------------------------------------------
    // GET /items/{itemId}/reviews
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("GET /items/{itemId}/reviews")
    class GetByItem {

        @Test
        @DisplayName("Returns 200 with a list of reviews")
        void getByItem_validItem_returnsList() throws Exception {
            when(itemReviewService.getByItem(1L))
                    .thenReturn(List.of(stubResponse(4, "Good"), stubResponse(5, "Great")));

            mockMvc.perform(get("/items/1/reviews"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Returns 200 with empty list when item has no reviews")
        void getByItem_noReviews_returnsEmptyList() throws Exception {
            when(itemReviewService.getByItem(1L)).thenReturn(List.of());

            mockMvc.perform(get("/items/1/reviews"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Returns 404 when item does not exist")
        void getByItem_itemNotFound_returns404() throws Exception {
            when(itemReviewService.getByItem(99L)).thenThrow(ItemNotFoundException.id(99L));

            mockMvc.perform(get("/items/99/reviews"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM-404-001"));
        }
    }

    // ---------------------------------------------------------------
    // GET /reviews/{reviewId}
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("GET /reviews/{reviewId}")
    class GetById {

        @Test
        @DisplayName("Returns 200 with the review when found")
        void getById_found_returns200() throws Exception {
            when(itemReviewService.getById(100L)).thenReturn(stubResponse(4, "Good"));

            mockMvc.perform(get("/reviews/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.rating").value(4));
        }

        @Test
        @DisplayName("Returns 404 when review does not exist")
        void getById_notFound_returns404() throws Exception {
            when(itemReviewService.getById(999L))
                    .thenThrow(ItemReviewNotFoundException.id(999L));

            mockMvc.perform(get("/reviews/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM_REVIEW-404-001"));
        }
    }

    // ---------------------------------------------------------------
    // PUT /reviews/{reviewId}/update
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("PUT /reviews/{reviewId}/update")
    class UpdateReview {

        @Test
        @DisplayName("Returns 200 with updated review")
        void update_validRequest_returns200() throws Exception {
            ItemReviewRequest request = new ItemReviewRequest(2, "Changed my mind.");
            when(itemReviewService.update(eq(100L), any(User.class), any(ItemReviewRequest.class)))
                    .thenReturn(stubResponse(2, "Changed my mind."));

            mockMvc.perform(put("/reviews/100/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating").value(2))
                    .andExpect(jsonPath("$.review").value("Changed my mind."));
        }

        @Test
        @DisplayName("Returns 404 when review does not exist")
        void update_reviewNotFound_returns404() throws Exception {
            ItemReviewRequest request = new ItemReviewRequest(3, "Update attempt");

            when(itemReviewService.update(eq(999L), any(User.class), any(ItemReviewRequest.class)))
                    .thenThrow(ItemReviewNotFoundException.id(999L));

            mockMvc.perform(put("/reviews/999/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM_REVIEW-404-001"));
        }

        @Test
        @DisplayName("Returns 400 when rating is null in update request")
        void update_nullRating_returns400() throws Exception {
            String body = "{\"review\":\"Missing rating\"}";

            mockMvc.perform(put("/reviews/100/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));
        }
    }

    // ---------------------------------------------------------------
    // DELETE /reviews/{reviewId}/delete
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /reviews/{reviewId}/delete")
    class DeleteReview {

        @Test
        @DisplayName("Returns 204 No Content on successful deletion")
        void delete_existingReview_returns204() throws Exception {
            doNothing().when(itemReviewService).delete(eq(100L), any(User.class));

            mockMvc.perform(delete("/reviews/100/delete"))
                    .andExpect(status().isNoContent());

            verify(itemReviewService).delete(eq(100L), any(User.class));
        }

        @Test
        @DisplayName("Returns 404 when review to delete does not exist")
        void delete_reviewNotFound_returns404() throws Exception {
            doThrow(ItemReviewNotFoundException.id(999L))
                    .when(itemReviewService).delete(eq(999L), any(User.class));

            mockMvc.perform(delete("/reviews/999/delete"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ITEM_REVIEW-404-001"));
        }
    }
}