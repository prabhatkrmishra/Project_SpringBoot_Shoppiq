package com.pkmprojects.shoppiq.exception.auth;

import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * <strong>Spring Boot Concept:</strong> Concrete leaf exception in the
 * {@code auth} branch for JWT-specific authentication failures.
 *
 * <p>Thrown when JWT authentication fails. Typical causes include expired
 * JWT, malformed JWT, invalid signature, or unsupported JWT.</p>
 *
 * <p>Unlike most leaf exceptions that hard-code a single error code,
 * this class accepts any {@link ErrorCode} at construction time, making
 * it reusable for multiple JWT failure scenarios
 * ({@code INVALID_JWT}, {@code JWT_EXPIRED}) without creating a
 * separate class for each.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public class JwtAuthenticationException extends AuthenticationException {

    /**
     * Creates a JWT authentication exception.
     *
     * @param errorCode authentication error code
     * @param detail    detailed error description
     */
    public JwtAuthenticationException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

}
