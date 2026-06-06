package com.crud.project.shoppiq.auth.oauth2;

import com.crud.project.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.crud.project.shoppiq.models.User;
import com.crud.project.shoppiq.repositories.UserRepository;
import com.crud.project.shoppiq.services.RolesService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final RolesService rolesService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private long expirationTime;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtAuthenticationUtils jwtAuthenticationUtils, RolesService rolesService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.rolesService = rolesService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String email = oidcUser.getEmail();

            User user = userRepository.findUserByUsername(email).orElseGet(() -> createGoogleUser(email));

            String jwt = jwtAuthenticationUtils.generateToken(user, expirationTime);

            response.addCookie(jwtAuthenticationUtils.buildJwtCookie(jwt, (int) (expirationTime / 1000)));

            /*
             * OAuth2 login temporarily creates an HttpSession that stores an
             * OAuth2AuthenticationToken. Once a JWT has been issued, the session
             * is no longer required because authentication for all subsequent
             * requests is performed using the JWT cookie.
             *
             * Invalidating the session prevents stale OAuth2 authentication from
             * remaining in the SecurityContext and ensures the application operates
             * as a stateless JWT-based system after login.
             */
            request.getSession().invalidate();

            response.sendRedirect("/allitems");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public User createGoogleUser(String email) {
        try {
            User newGoogleUser = new User();

            newGoogleUser.setUsername(email);
            newGoogleUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            newGoogleUser.setRoles(Set.of(rolesService.getCustomerRole()));

            return userRepository.save(newGoogleUser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}