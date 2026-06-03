package com.crud.project.shoppiq.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for the login request payload.
 *
 * <p>Carries the user credentials submitted by the client. The
 * {@code rememberMe} flag controls cookie persistence on the server side:
 * when {@code true} the login endpoint sets a {@code Max-Age} attribute on
 * the JWT cookie so it survives browser restarts; when {@code false} the
 * cookie is a session cookie and is discarded when the browser closes.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequest {
    private String username;
    private String password;
    private Boolean rememberMe;
}