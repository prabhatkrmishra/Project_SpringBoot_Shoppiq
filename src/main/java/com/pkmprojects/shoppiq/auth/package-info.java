/**
 * Authentication and authorization module for the Shoppiq application.
 *
 * <p>This package contains the complete authentication layer, including
 * JWT-based stateless authentication, Google OAuth2 login integration,
 * Spring Security filter chain configuration, and supporting utilities.
 * The module handles credential validation, token generation, cookie
 * management, authority mapping, and session handling for both email/password
 * and OAuth2 login flows.</p>
 *
 * <p>Architecturally, the auth module is decoupled from the rest of the
 * application through well-defined interfaces. The
 * {@link com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter} processes
 * incoming requests, the {@link com.pkmprojects.shoppiq.auth.service.AuthService}
 * orchestrates the login flow, and the
 * {@link com.pkmprojects.shoppiq.auth.oauth2.OAuth2SuccessHandler} handles
 * OAuth2 callback processing. All authentication failures are converted
 * into RFC 9457 Problem Detail responses by the global exception handler.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.auth;
