package com.pkmprojects.shoppiq.auth.intergration;

import com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter;
import com.pkmprojects.shoppiq.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler;
import com.pkmprojects.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.pkmprojects.shoppiq.auth.utils.JwtCookieFactory;
import com.pkmprojects.shoppiq.auth.entrypoint.ShoppiqAuthenticationEntryPoint;
import com.pkmprojects.shoppiq.auth.handler.ShoppiqAccessDeniedHandler;
import com.pkmprojects.shoppiq.config.SecurityConfig;
import com.pkmprojects.shoppiq.config.JacksonConfig;
import com.pkmprojects.shoppiq.controller.RolesController;
import com.pkmprojects.shoppiq.entity.Role;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
import com.pkmprojects.shoppiq.util.http.ProblemDetailResponseWriter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end security exception-handling tests.
 *
 * <p>
 * These tests run requests through the <em>real</em> {@link SecurityConfig}
 * filter chain — including the real {@link JwtAuthenticationFilter} and
 * {@link JwtAuthenticationUtils} — rather than mocking security around the
 * edges. This specifically exercises the Milestone 1.4 fix where JWT failures
 * are written directly create the filter instead of being thrown past the
 * point where anything could catch them (see {@link JwtAuthenticationFilter}
 * class-level documentation), and confirms that authentication/authorization
 * failures raised by Spring Security's own filters still reach
 * {@link ShoppiqAuthenticationEntryPoint} and {@link ShoppiqAccessDeniedHandler}
 * as expected.
 * </p>
 *
 * <p>
 * No database is required: {@link UserRepository} and {@link RolesService}
 * are mocked, and {@link OAuth2SuccessHandler} is mocked since the OAuth2
 * login flow itself is out of scope here.
 * </p>
 *
 * <h2>Why URL-level security is used instead of {@code @PreAuthorize}</h2>
 * <p>
 * {@code @WebMvcTest} slices do not reliably activate the method-security AOP
 * post-processor for inner {@code @RestController} classes, even when
 * {@code @EnableMethodSecurity} is present. The 403 access-denied path is
 * therefore exercised via {@link RolesController#getAllRole()}, a real
 * production endpoint that {@link SecurityConfig} already restricts to
 * {@code ROLE_ADMIN} at the URL level — no AOP proxy required.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@WebMvcTest(controllers = {SecurityExceptionIntegrationTest.SecuredTestController.class, RolesController.class})
@Import({
        SecurityConfig.class,
        JacksonConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationUtils.class,
        JwtCookieFactory.class,
        ShoppiqAuthenticationEntryPoint.class,
        ShoppiqAccessDeniedHandler.class,
        ProblemDetailResponseWriter.class
})
@ActiveProfiles("test")
@DisplayName("Security Exception Integration Tests")
class SecurityExceptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtAuthenticationUtils jwtAuthenticationUtils;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository
            httpCookieOAuth2AuthorizationRequestRepository;

    @Test
    @DisplayName("Unauthenticated request to a protected endpoint returns RFC9457 401")
    void unauthenticatedRequest_returnsUnauthorizedProblemDetail() throws Exception {
        mockMvc.perform(get("/secure/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.errorCode").value("AUTH-401-001"));
    }

    @Test
    @DisplayName("Malformed JWT cookie is rejected by the filter itself as RFC9457 401")
    void malformedJwtCookie_isHandledDirectlyByTheFilter() throws Exception {
        mockMvc.perform(get("/secure/ping").cookie(new Cookie("jwt", "not-a-real-jwt")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.errorCode").value("AUTH-401-003"))
                .andExpect(jsonPath("$.instance").value("/secure/ping"));
    }

    @Test
    @DisplayName("Valid JWT but insufficient role returns RFC9457 403 via ShoppiqAccessDeniedHandler")
    void authenticatedWithoutRequiredRole_returnsForbiddenProblemDetail() throws Exception {
        Role customerRole = new Role();
        customerRole.setRoleName("ROLE_CUSTOMER");

        User user = User.builder().name("Alice").email("alice@example.com").username("alice")
                .password("hashed-password").enabled(true)
                .tokenVersion(0).roles(Set.of(customerRole)).build();

        /* The id field is declared in BaseEntity with no setter and no @Builder support,
         * so it must be set via reflection to ensure generateToken() embeds a non-null
         * userId claim and the filter's userRepository.findById() call can be matched.
         */
        java.lang.reflect.Field idField = com.pkmprojects.shoppiq.audit.BaseEntity.class
                .getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, 1L);

        String token = jwtAuthenticationUtils.generateToken(user, 60_000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        /* GET /roles/all is restricted to ROLE_ADMIN in SecurityConfig via URL-level rules.
         * A CUSTOMER-role user triggers AccessDeniedException -> ShoppiqAccessDeniedHandler -> /error.
         */
        mockMvc.perform(get("/roles/all").cookie(new Cookie("jwt", token)))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/error"));
    }

    @RestController
    static class SecuredTestController {

        @GetMapping("/secure/ping")
        public String ping() {
            return "pong";
        }
    }
}