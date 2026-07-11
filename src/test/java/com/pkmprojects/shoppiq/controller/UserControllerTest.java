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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.dto.response.UserResponse;
import com.pkmprojects.shoppiq.dto.user.ChangePasswordRequest;
import com.pkmprojects.shoppiq.dto.user.UpdateProfileRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.UserService;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import org.junit.jupiter.api.AfterEach;
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

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.doNothing;

@WebMvcTest(UserController.class)
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
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                .name("Customer User")
                .username("customeruser")
                .email("customer@example.com")
                .password("hashed")
                .enabled(true)
                .build();

        try {
            Field idField = authenticatedUser.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(authenticatedUser, 1L);
        } catch (Exception ignored) {
        }

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
    @DisplayName("GET /user/profile")
    class GetProfile {

        @Test
        @DisplayName("Returns 200 with user profile when authenticated")
        void getProfile_authenticated_returns200() throws Exception {
            UserResponse profile = UserResponse.of(authenticatedUser, null, true);

            when(userService.getProfile(any())).thenReturn(profile);

            mockMvc.perform(get("/user/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Customer User"))
                    .andExpect(jsonPath("$.email").value("customer@example.com"))
                    .andExpect(jsonPath("$.username").value("customeruser"));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void getProfile_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/user/profile"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /user/profile")
    class UpdateProfile {

        @Test
        @DisplayName("Returns 200 with updated profile when name is valid")
        void updateProfile_validName_returns200() throws Exception {
            UpdateProfileRequest request = new UpdateProfileRequest("Updated Name");
            UserResponse updated = UserResponse.of(authenticatedUser, null, true);

            doNothing().when(userService).updateProfile(any(), any());
            when(userService.getProfile(any())).thenReturn(updated);

            mockMvc.perform(put("/user/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Customer User"));

            verify(userService).updateProfile(any(), any());
        }

        @Test
        @DisplayName("Returns 400 when name is empty")
        void updateProfile_emptyName_returns400() throws Exception {
            UpdateProfileRequest request = new UpdateProfileRequest("");

            mockMvc.perform(put("/user/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void updateProfile_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();

            UpdateProfileRequest request = new UpdateProfileRequest("Name");

            mockMvc.perform(put("/user/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /user/password")
    class ChangePassword {

        @Test
        @DisplayName("Returns 200 when password is changed")
        void changePassword_validRequest_returns200() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("currentPass", "NewPass123!", "NewPass123!");

            doNothing().when(userService).changePassword(any(), any());

            mockMvc.perform(put("/user/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).changePassword(any(), any());
        }

        @Test
        @DisplayName("Returns 200 when current password is null (OAuth account)")
        void changePassword_noCurrentPassword_returns200() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest(null, "NewPass123!", "NewPass123!");

            doNothing().when(userService).changePassword(any(), any());

            mockMvc.perform(put("/user/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Returns 400 when new password is too short")
        void changePassword_shortNewPassword_returns400() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("currentPass", "123", "123");

            mockMvc.perform(put("/user/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 400 when new password is blank")
        void changePassword_blankNewPassword_returns400() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("currentPass", "", "");

            mockMvc.perform(put("/user/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void changePassword_unauthenticated_returns401() throws Exception {
            SecurityContextHolder.clearContext();

            ChangePasswordRequest request = new ChangePasswordRequest("currentPass", "NewPass123!", "NewPass123!");

            mockMvc.perform(put("/user/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
