package com.pkmprojects.shoppiq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.oauth2.OAuthReturnUrlFilter;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.config.JacksonConfig;
import com.pkmprojects.shoppiq.config.SecurityConfig;
import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import com.pkmprojects.shoppiq.dto.address.UpdateAddressRequest;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.AddressAccessDeniedException;
import com.pkmprojects.shoppiq.exception.AddressNotFoundException;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.AddressService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Controller-slice tests for {@link UserAddressController}.
 *
 * <p>
 * Uses {@code @WebMvcTest} to load only the web layer; {@link AddressService}
 * is mocked. The real {@link SecurityConfig} and JWT infrastructure are
 * imported so the security filter chain functions correctly.
 * </p>
 *
 * <p>
 * Tests set the {@link org.springframework.security.core.context.SecurityContext}
 * with a {@link UsernamePasswordAuthenticationToken} carrying the application
 * {@link User} entity as principal, matching how {@link JwtAuthenticationFilter}
 * configures authentication at runtime.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@WebMvcTest(UserAddressController.class)
@Import({
        SecurityConfig.class,
        JacksonConfig.class,
        GlobalExceptionHandler.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationUtils.class,
        JwtCookieFactory.class,
        ShoppiqAuthenticationEntryPoint.class,
        ShoppiqAccessDeniedHandler.class,
        ProblemDetailResponseWriter.class,
        OAuthReturnUrlFilter.class
})
@ActiveProfiles("test")
@DisplayName("UserAddressController Tests")
class UserAddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

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

    private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    private static AddressResponse stubResponse(long id, boolean isDefault) {
        return new AddressResponse(
                id, "Home", "Alice Smith", "9876543210",
                "221B Baker Street", "Near Chandni Chowk",
                "Delhi", "Delhi", "110001", "India",
                isDefault, NOW, NOW
        );
    }

    private static CreateAddressRequest validCreateRequest(boolean isDefault) {
        return new CreateAddressRequest(
                "Home", "Alice Smith", "9876543210",
                "221B Baker Street", "Near Chandni Chowk",
                "Delhi", "Delhi", "110001", "India", isDefault
        );
    }

    private static UpdateAddressRequest validUpdateRequest(boolean isDefault) {
        return new UpdateAddressRequest(
                "Office", "Alice Smith", "9876543210",
                "42 Connaught Place", null,
                "Delhi", "Delhi", "110001", "India", isDefault
        );
    }

    private void authenticateUser() {
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
    // POST /user/address/create
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("POST /user/address/create")
    class CreateAddress {

        @Test
        @DisplayName("Returns 201 Created with address body on success")
        void create_validRequest_returns201() throws Exception {
            authenticateUser();
            AddressResponse response = stubResponse(1L, false);
            when(addressService.create(any(User.class), any(CreateAddressRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/user/address/create").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest(false))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.label").value("Home"))
                    .andExpect(jsonPath("$.fullName").value("Alice Smith"))
                    .andExpect(jsonPath("$.city").value("Delhi"))
                    .andExpect(jsonPath("$.default").value(false));
        }

        @Test
        @DisplayName("Returns 201 with default=true when address is marked as default")
        void create_asDefault_returnsDefaultTrue() throws Exception {
            authenticateUser();
            AddressResponse response = stubResponse(2L, true);
            when(addressService.create(any(User.class), any(CreateAddressRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/user/address/create").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest(true))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.default").value(true));
        }

        @Test
        @DisplayName("Returns 400 when label is blank")
        void create_blankLabel_returns400() throws Exception {
            authenticateUser();
            String body = """
                    {"label":"","fullName":"Alice","phone":"9876543210",
                    "line1":"Street","city":"Delhi","state":"Delhi",
                    "postalCode":"110001","country":"India","default":false}
                    """;

            mockMvc.perform(post("/user/address/create").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(addressService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Returns 400 when fullName is missing")
        void create_missingFullName_returns400() throws Exception {
            authenticateUser();
            String body = """
                    {"label":"Home","phone":"9876543210",
                    "line1":"Street","city":"Delhi","state":"Delhi",
                    "postalCode":"110001","country":"India","default":false}
                    """;

            mockMvc.perform(post("/user/address/create").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(addressService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Returns 400 when phone fails pattern validation")
        void create_invalidPhone_returns400() throws Exception {
            authenticateUser();
            String body = """
                    {"label":"Home","fullName":"Alice Smith","phone":"abc",
                    "line1":"Street","city":"Delhi","state":"Delhi",
                    "postalCode":"110001","country":"India","default":false}
                    """;

            mockMvc.perform(post("/user/address/create").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(addressService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Returns 400 when line1 is blank")
        void create_blankLine1_returns400() throws Exception {
            authenticateUser();
            String body = """
                    {"label":"Home","fullName":"Alice Smith","phone":"9876543210",
                    "line1":"","city":"Delhi","state":"Delhi",
                    "postalCode":"110001","country":"India","default":false}
                    """;

            mockMvc.perform(post("/user/address/create").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(addressService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Returns 400 when city is missing")
        void create_missingCity_returns400() throws Exception {
            authenticateUser();
            String body = """
                    {"label":"Home","fullName":"Alice Smith","phone":"9876543210",
                    "line1":"Street","state":"Delhi",
                    "postalCode":"110001","country":"India","default":false}
                    """;

            mockMvc.perform(post("/user/address/create").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(addressService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Returns 400 when country is blank")
        void create_blankCountry_returns400() throws Exception {
            authenticateUser();
            String body = """
                    {"label":"Home","fullName":"Alice Smith","phone":"9876543210",
                    "line1":"Street","city":"Delhi","state":"Delhi",
                    "postalCode":"110001","country":"","default":false}
                    """;

            mockMvc.perform(post("/user/address/create").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(addressService, never()).create(any(), any());
        }

        @Test
        @DisplayName("Returns 401 when request is unauthenticated")
        void create_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/user/address/create").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest(false))))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------------------------------------------------
    // GET /user/address/get
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("GET /user/address/get")
    class GetAllAddresses {

        @Test
        @DisplayName("Returns 200 with address list when user has addresses")
        void getAll_withAddresses_returns200() throws Exception {
            authenticateUser();
            List<AddressResponse> list = List.of(
                    stubResponse(1L, true),
                    stubResponse(2L, false)
            );
            when(addressService.getAll(any(User.class))).thenReturn(list);

            mockMvc.perform(get("/user/address/get/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].default").value(true))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].default").value(false));
        }

        @Test
        @DisplayName("Returns 200 with empty array when user has no addresses")
        void getAll_noAddresses_returnsEmptyArray() throws Exception {
            authenticateUser();
            when(addressService.getAll(any(User.class))).thenReturn(List.of());

            mockMvc.perform(get("/user/address/get/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void getAll_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/user/address/get/all"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------------------------------------------------
    // GET /user/address/get/{id}
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("GET /user/address/get/{id}")
    class GetAddressById {

        @Test
        @DisplayName("Returns 200 with address body when found and owned")
        void getById_found_returns200() throws Exception {
            authenticateUser();
            AddressResponse response = stubResponse(5L, false);
            when(addressService.getById(any(User.class), eq(5L))).thenReturn(response);

            mockMvc.perform(get("/user/address/get/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.label").value("Home"))
                    .andExpect(jsonPath("$.postalCode").value("110001"));
        }

        @Test
        @DisplayName("Returns 404 when address does not exist")
        void getById_notFound_returns404() throws Exception {
            authenticateUser();
            when(addressService.getById(any(User.class), eq(99L)))
                    .thenThrow(AddressNotFoundException.id(99L));

            mockMvc.perform(get("/user/address/get/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ADDRESS-404-001"));
        }

        @Test
        @DisplayName("Returns 403 when address belongs to another user")
        void getById_wrongOwner_returns403() throws Exception {
            authenticateUser();
            when(addressService.getById(any(User.class), eq(5L)))
                    .thenThrow(AddressAccessDeniedException.forAddress(5L));

            mockMvc.perform(get("/user/address/get/5"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("ADDRESS-403-001"));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void getById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/user/address/get/5"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------------------------------------------------
    // PUT /user/address/update/{id}
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("PUT /user/address/update/{id}")
    class UpdateAddress {

        @Test
        @DisplayName("Returns 200 with updated address body on success")
        void update_valid_returns200() throws Exception {
            authenticateUser();
            AddressResponse response = new AddressResponse(
                    5L, "Office", "Alice Smith", "9876543210",
                    "42 Connaught Place", null,
                    "Delhi", "Delhi", "110001", "India",
                    false, NOW, NOW
            );
            when(addressService.update(any(User.class), eq(5L), any(UpdateAddressRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/user/address/update/5").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest(false))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.label").value("Office"))
                    .andExpect(jsonPath("$.line1").value("42 Connaught Place"));
        }

        @Test
        @DisplayName("Returns 400 when label is blank in update request")
        void update_blankLabel_returns400() throws Exception {
            authenticateUser();
            String body = """
                    {"label":"","fullName":"Alice","phone":"9876543210",
                    "line1":"Street","city":"Delhi","state":"Delhi",
                    "postalCode":"110001","country":"India","default":false}
                    """;

            mockMvc.perform(put("/user/address/update/5").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(addressService, never()).update(any(), any(), any());
        }

        @Test
        @DisplayName("Returns 400 when phone is invalid in update request")
        void update_invalidPhone_returns400() throws Exception {
            authenticateUser();
            String body = """
                    {"label":"Home","fullName":"Alice","phone":"INVALID",
                    "line1":"Street","city":"Delhi","state":"Delhi",
                    "postalCode":"110001","country":"India","default":false}
                    """;

            mockMvc.perform(put("/user/address/update/5").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION-400-001"));

            verify(addressService, never()).update(any(), any(), any());
        }

        @Test
        @DisplayName("Returns 404 when address does not exist")
        void update_notFound_returns404() throws Exception {
            authenticateUser();
            when(addressService.update(any(User.class), eq(99L), any(UpdateAddressRequest.class)))
                    .thenThrow(AddressNotFoundException.id(99L));

            mockMvc.perform(put("/user/address/update/99").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest(false))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ADDRESS-404-001"));
        }

        @Test
        @DisplayName("Returns 403 when address belongs to another user")
        void update_wrongOwner_returns403() throws Exception {
            authenticateUser();
            when(addressService.update(any(User.class), eq(5L), any(UpdateAddressRequest.class)))
                    .thenThrow(AddressAccessDeniedException.forAddress(5L));

            mockMvc.perform(put("/user/address/update/5").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest(false))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("ADDRESS-403-001"));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void update_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put("/user/address/update/5").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateRequest(false))))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------------------------------------------------
    // DELETE /user/address/delete/{id}
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /user/address/delete/{id}")
    class DeleteAddress {

        @Test
        @DisplayName("Returns 204 No Content on successful deletion")
        void delete_owned_returns204() throws Exception {
            authenticateUser();
            doNothing().when(addressService).delete(any(User.class), eq(5L));

            mockMvc.perform(delete("/user/address/delete/5").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(addressService).delete(any(User.class), eq(5L));
        }

        @Test
        @DisplayName("Returns 404 when address does not exist")
        void delete_notFound_returns404() throws Exception {
            authenticateUser();
            doThrow(AddressNotFoundException.id(99L))
                    .when(addressService).delete(any(User.class), eq(99L));

            mockMvc.perform(delete("/user/address/delete/99").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ADDRESS-404-001"));
        }

        @Test
        @DisplayName("Returns 403 when address belongs to another user")
        void delete_wrongOwner_returns403() throws Exception {
            authenticateUser();
            doThrow(AddressAccessDeniedException.forAddress(5L))
                    .when(addressService).delete(any(User.class), eq(5L));

            mockMvc.perform(delete("/user/address/delete/5").with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("ADDRESS-403-001"));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void delete_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete("/user/address/delete/5").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ---------------------------------------------------------------
    // PUT /user/address/default/{id}
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("PUT /user/address/default/{id}")
    class SetDefaultAddress {

        @Test
        @DisplayName("Returns 200 with default=true on success")
        void setDefault_valid_returns200() throws Exception {
            authenticateUser();
            AddressResponse response = stubResponse(5L, true);
            when(addressService.setDefault(any(User.class), eq(5L))).thenReturn(response);

            mockMvc.perform(put("/user/address/default/5").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.default").value(true));
        }

        @Test
        @DisplayName("Returns 404 when address does not exist")
        void setDefault_notFound_returns404() throws Exception {
            authenticateUser();
            when(addressService.setDefault(any(User.class), eq(99L)))
                    .thenThrow(AddressNotFoundException.id(99L));

            mockMvc.perform(put("/user/address/default/99").with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ADDRESS-404-001"));
        }

        @Test
        @DisplayName("Returns 403 when address belongs to another user")
        void setDefault_wrongOwner_returns403() throws Exception {
            authenticateUser();
            when(addressService.setDefault(any(User.class), eq(5L)))
                    .thenThrow(AddressAccessDeniedException.forAddress(5L));

            mockMvc.perform(put("/user/address/default/5").with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("ADDRESS-403-001"));
        }

        @Test
        @DisplayName("Returns 401 when unauthenticated")
        void setDefault_unauthenticated_returns401() throws Exception {
            mockMvc.perform(put("/user/address/default/5").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }
}