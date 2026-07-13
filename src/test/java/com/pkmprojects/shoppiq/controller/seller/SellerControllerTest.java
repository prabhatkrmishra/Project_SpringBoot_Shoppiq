package com.pkmprojects.shoppiq.controller.seller;

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
import com.pkmprojects.shoppiq.dto.seller.request.SellerProfileUpdateRequest;
import com.pkmprojects.shoppiq.dto.seller.request.SellerRegistrationRequest;
import com.pkmprojects.shoppiq.dto.seller.response.SellerResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.enums.SellerStatus;
import com.pkmprojects.shoppiq.enums.VerificationStatus;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.service.seller.SellerService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(SellerController.class)
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
@DisplayName("SellerController Tests")
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SellerService sellerService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private static final Long USER_ID = 1L;
    private static final Long SELLER_ID = 1L;
    private static final String BUSINESS_NAME = "Test Shop";
    private static final String BUSINESS_EMAIL = "shop@test.com";
    private static final String PHONE = "1234567890";
    private static final String GST_NUMBER = "GST12345";
    private static final String PAN_NUMBER = "ABCDE1234F";

    private User authenticatedUser;

    private static SellerResponse stubSellerResponse() {
        return new SellerResponse(
                SELLER_ID,
                USER_ID,
                BUSINESS_NAME,
                BUSINESS_EMAIL,
                PHONE,
                GST_NUMBER,
                PAN_NUMBER,
                null,
                VerificationStatus.PENDING,
                SellerStatus.INACTIVE,
                BigDecimal.ZERO,
                null,
                LocalDateTime.now()
        );
    }

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("POST /seller/register")
    class RegisterSeller {

        @Test
        @DisplayName("Returns 201 Created with response body on success")
        void register_validRequest_returns201() throws Exception {
            SellerRegistrationRequest request = new SellerRegistrationRequest(
                    BUSINESS_NAME, BUSINESS_EMAIL, PHONE, GST_NUMBER, PAN_NUMBER
            );

            when(sellerService.register(any(SellerRegistrationRequest.class), any(User.class)))
                    .thenReturn(stubSellerResponse());

            mockMvc.perform(post("/seller/register").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.businessName").value(BUSINESS_NAME))
                    .andExpect(jsonPath("$.businessEmail").value(BUSINESS_EMAIL));
        }

        @Test
        @DisplayName("Returns 400 when business name is blank")
        void register_blankBusinessName_returns400() throws Exception {
            SellerRegistrationRequest request = new SellerRegistrationRequest(
                    "", BUSINESS_EMAIL, PHONE, GST_NUMBER, PAN_NUMBER
            );

            mockMvc.perform(post("/seller/register").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(sellerService, never()).register(any(), any());
        }
    }

    @Nested
    @DisplayName("GET /seller/profile")
    class GetProfile {

        @Test
        @DisplayName("Returns 200 with seller profile")
        void getProfile_returns200() throws Exception {
            when(sellerService.getProfile(any(User.class)))
                    .thenReturn(stubSellerResponse());

            mockMvc.perform(get("/seller/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.businessName").value(BUSINESS_NAME))
                    .andExpect(jsonPath("$.businessEmail").value(BUSINESS_EMAIL));
        }
    }

    @Nested
    @DisplayName("PUT /seller/update")
    class UpdateProfile {

        @Test
        @DisplayName("Returns 200 with updated profile on success")
        void update_validRequest_returns200() throws Exception {
            SellerProfileUpdateRequest request = new SellerProfileUpdateRequest(
                    "Updated Shop", BUSINESS_EMAIL, PHONE, GST_NUMBER, PAN_NUMBER, null
            );

            SellerResponse updatedResponse = new SellerResponse(
                    SELLER_ID, USER_ID, "Updated Shop", BUSINESS_EMAIL, PHONE,
                    GST_NUMBER, PAN_NUMBER, null, VerificationStatus.PENDING,
                    SellerStatus.INACTIVE, BigDecimal.ZERO, null, LocalDateTime.now()
            );

            when(sellerService.updateProfile(any(SellerProfileUpdateRequest.class), any(User.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/seller/update").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.businessName").value("Updated Shop"));
        }
    }

    @Nested
    @DisplayName("DELETE /seller/delete")
    class DeleteProfile {

        @Test
        @DisplayName("Returns 200 on successful deletion")
        void delete_returns200() throws Exception {
            doNothing().when(sellerService).deleteProfile(any(User.class));

            mockMvc.perform(delete("/seller/delete").with(csrf()))
                    .andExpect(status().isOk());

            verify(sellerService).deleteProfile(any(User.class));
        }
    }
}
