/**
 * Authentication and authorization exception hierarchy.
 *
 * <p>This package contains exceptions for authentication failures (HTTP 401),
 * invalid credentials, JWT token errors, and OAuth2 session issues. These
 * exceptions are thrown during the login flow, JWT filter processing, and
 * OAuth2 callback handling. The global exception handler maps them to
 * RFC 9457 Problem Detail responses with the appropriate HTTP status.</p>
 *
 * <p>For authorization failures (HTTP 403), see the
 * {@link com.pkmprojects.shoppiq.exception.business.UnauthorizedOperationException}
 * hierarchy. Authentication exceptions indicate that the user is not
 * identified, while authorization exceptions indicate that the identified
 * user lacks permission.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.exception.auth;
