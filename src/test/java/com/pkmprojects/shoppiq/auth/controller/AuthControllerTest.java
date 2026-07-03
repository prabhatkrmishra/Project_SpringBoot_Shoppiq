package com.pkmprojects.shoppiq.auth.controller;

import com.pkmprojects.shoppiq.auth.dto.JwtRequest;
import com.pkmprojects.shoppiq.auth.dto.JwtResponse;
import com.pkmprojects.shoppiq.auth.oauth2.OAuthRegistrationCookieService;
import com.pkmprojects.shoppiq.auth.service.AuthService;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtAuthenticationUtils jwtAuthenticationUtils;

    @Mock
    private JwtCookieFactory jwtCookieFactory;

    @Mock
    private OAuthRegistrationCookieService registrationCookieService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private void setupMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /auth/logout clears JWT cookie and OAuth2 registration cookie")
    void logout_clearsAllCookies() throws Exception {
        setupMockMvc();

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));

        verify(authService).logout(any());
        verify(registrationCookieService).clear(any());
    }

    @Test
    @DisplayName("POST /auth/login returns 200 on valid credentials")
    void login_validCredentials_returnsOk() throws Exception {
        setupMockMvc();

        JwtResponse jwtResponse = new JwtResponse("Login successful");
        when(authService.login(any(JwtRequest.class), any())).thenReturn(jwtResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));

        verify(authService).login(any(JwtRequest.class), any());
    }

    @Test
    @DisplayName("POST /auth/login returns 400 on invalid credentials")
    void login_invalidCredentials_returnsBadRequest() throws Exception {
        setupMockMvc();

        when(authService.login(any(JwtRequest.class), any()))
                .thenThrow(new com.pkmprojects.shoppiq.exception.auth.InvalidCredentialException(
                        "Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"wronguser\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
