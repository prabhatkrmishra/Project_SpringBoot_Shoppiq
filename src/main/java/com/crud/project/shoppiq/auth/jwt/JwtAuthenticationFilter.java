package com.crud.project.shoppiq.auth.jwt;

import com.crud.project.shoppiq.auth.utils.JwtAuthenticationUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 * Intercepts every HTTP request, extracts JWT from Authorization header,
 * validates it, and sets authentication in SecurityContext if valid.
 * Runs before standard Spring Security filters.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationUtils jwtAuthenticationUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtAuthenticationUtils jwtAuthenticationUtils, UserDetailsService userDetailsService) {
        this.jwtAuthenticationUtils = jwtAuthenticationUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Main filter method executed once per request.
     * Reads Authorization header, extracts token, validates it,
     * loads user details, and creates an authenticated context.
     * <p>
     * SecurityContextHolder stores authentication information for the
     * current request. After successful JWT validation, an authenticated
     * UsernamePasswordAuthenticationToken is stored in the SecurityContext,
     * allowing Spring Security to identify the current user and enforce
     * role-based access control (@PreAuthorize, hasRole, etc.).
     * <p>
     * Called automatically by Spring Security filter chain.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            String username = null;
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                username = jwtAuthenticationUtils.getUsernameFromToken(token);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtAuthenticationUtils.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}