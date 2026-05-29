package com.crud.project.shoppiq.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for login request payload.
 * Contains credentials sent by client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequest {
    private String username;
    private String password;
    private Boolean rememberMe;
}