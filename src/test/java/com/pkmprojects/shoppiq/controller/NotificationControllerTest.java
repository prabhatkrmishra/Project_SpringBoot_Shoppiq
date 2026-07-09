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
import com.pkmprojects.shoppiq.dto.notification.NotificationPreferenceResponse;
import com.pkmprojects.shoppiq.dto.notification.UpdateNotificationPreferenceRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.service.NotificationService;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.UserService;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.AfterEach;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
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
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = User.builder()
                .name("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("hashed")
                .enabled(true)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /user/notifications")
    class GetPreferences {

        @Test
        @DisplayName("Returns 200 with preferences when authenticated")
        void getPreferences_authenticated_returns200() throws Exception {
            NotificationPreferenceResponse response = new NotificationPreferenceResponse(1L, true, true, false, true);

            when(notificationService.getPreferences(any())).thenReturn(response);

            mockMvc.perform(get("/user/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.orderUpdates").value(true))
                    .andExpect(jsonPath("$.accountSecurity").value(true))
                    .andExpect(jsonPath("$.promotions").value(false))
                    .andExpect(jsonPath("$.reviewsEngagement").value(true));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void getPreferences_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/user/notifications"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /user/notifications")
    class UpdatePreferences {

        @Test
        @DisplayName("Returns 200 with updated preferences when authenticated")
        void updatePreferences_authenticated_returns200() throws Exception {
            UpdateNotificationPreferenceRequest request = new UpdateNotificationPreferenceRequest(false, true, null, true);
            NotificationPreferenceResponse response = new NotificationPreferenceResponse(1L, false, true, true, true);

            when(notificationService.updatePreferences(any(), any())).thenReturn(response);

            mockMvc.perform(put("/user/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderUpdates").value(false))
                    .andExpect(jsonPath("$.accountSecurity").value(true))
                    .andExpect(jsonPath("$.reviewsEngagement").value(true));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void updatePreferences_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();

            UpdateNotificationPreferenceRequest request = new UpdateNotificationPreferenceRequest(null, null, null, null);

            mockMvc.perform(put("/user/notifications")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
