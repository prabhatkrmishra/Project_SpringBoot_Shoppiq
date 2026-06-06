package com.crud.project.shoppiq.auth.oauth2;

import com.crud.project.shoppiq.auth.utils.JwtAuthenticationUtils;
import com.crud.project.shoppiq.auth.dto.OauthResponseDto;
import com.crud.project.shoppiq.exceptions.InvalidOidcUserException;
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
import java.util.Optional;

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
            if (oidcUser == null) {
                throw new InvalidOidcUserException("Oidc response user is null");
            }

            Optional<User> existingUser = userRepository.findUserByEmail(oidcUser.getEmail());
            if (existingUser.isPresent()) {

                String jwt = jwtAuthenticationUtils.generateToken(existingUser.get(), expirationTime);
                response.addCookie(jwtAuthenticationUtils.buildJwtCookie(jwt, (int) (expirationTime / 1000)));

                request.getSession().invalidate();

                response.sendRedirect("/allitems");

                return;
            }

            /*
             * User authenticated successfully with Google but does not yet
             * have a local application account.
             *
             * Store the verified Google profile temporarily so the user can
             * complete registration by choosing a username/password.
             */
            request.getSession(true).setAttribute("oauth_user", new OauthResponseDto(
                    oidcUser.getFullName(), oidcUser.getEmail()));

            response.sendRedirect("/complete-profile");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}