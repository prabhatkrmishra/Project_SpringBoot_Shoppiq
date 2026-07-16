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
import com.pkmprojects.shoppiq.entity.Role;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler;
import com.pkmprojects.shoppiq.repository.UserRepository;
import com.pkmprojects.shoppiq.service.RolesService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(RolesController.class)
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
@DisplayName("RolesController Tests")
class RolesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RolesService rolesService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = User.builder()
                .name("Admin User")
                .username("adminuser")
                .email("admin@example.com")
                .password("hashed")
                .enabled(true)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /roles/all")
    class GetAllRoles {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with list of roles")
        void getAllRoles_returnsList() throws Exception {
            Role role1 = new Role(1L, "ROLE_CUSTOMER");
            Role role2 = new Role(2L, "ROLE_SELLER");

            when(rolesService.getAllExistingRoles())
                    .thenReturn(List.of(role1, role2));

            mockMvc.perform(get("/roles/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].roleName").value("ROLE_CUSTOMER"))
                    .andExpect(jsonPath("$[1].roleName").value("ROLE_SELLER"));
        }
    }

    @Nested
    @DisplayName("POST /roles/create/{roleName}")
    class CreateRole {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Returns 200 with created role on success")
        void createRole_validName_returns200() throws Exception {
            Role createdRole = new Role(3L, "ROLE_MODERATOR");

            when(rolesService.createNewRole("MODERATOR"))
                    .thenReturn(createdRole);

            mockMvc.perform(post("/roles/create/MODERATOR").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roleName").value("ROLE_MODERATOR"));
        }
    }
}
