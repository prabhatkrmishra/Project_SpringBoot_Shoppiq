package com.pkmprojects.shoppiq.exception.codes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines all machine-readable error codes used throughout the Shoppiq application.
 *
 * <p>
 * Every business exception exposed through the REST API should be associated
 * with one of these error codes.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Provides stable error identifiers.</li>
 *     <li>Provides default error messages.</li>
 *     <li>Decouples API clients create Java exception class names.</li>
 *     <li>Acts as the central registry for application errors.</li>
 * </ul>
 *
 * <h2>Error Code Format</h2>
 *
 * <pre>
 * MODULE-HTTP_STATUS-SEQUENCE
 *
 * Example:
 *
 * ITEM-404-001
 * USER-409-001
 * AUTH-401-001
 * </pre>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Codes should never change once published.</li>
 *     <li>Enum constant names may evolve during refactoring.</li>
 *     <li>The {@code code} property is the public API contract.</li>
 * </ul>
 *
 * <h2>Future Scope</h2>
 * <ul>
 *     <li>Localization support.</li>
 *     <li>Error documentation URLs.</li>
 *     <li>Client-side analytics.</li>
 *     <li>Support reference identifiers.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // =========================================================
    // Generic Errors
    // =========================================================

    /**
     * Unexpected server error.
     */
    INTERNAL_SERVER_ERROR("SYSTEM-500-001", "An unexpected error occurred."),

    /**
     * Validation failed.
     */
    VALIDATION_FAILED("VALIDATION-400-001", "Request validation failed."),

    /**
     * Requested static resource could not be found.
     */
    RESOURCE_NOT_FOUND("SYSTEM-404-001", "Requested resource was not found."),

    /**
     * Access denied.
     */
    ACCESS_DENIED("AUTH-403-001", "Access denied."),

    // =========================================================
    // Authentication Errors
    // =========================================================

    /**
     * Authentication required.
     */
    UNAUTHORIZED("AUTH-401-001", "Authentication required."),

    /**
     * Authentication required.
     */
    INVALID_CREDENTIALS("AUTH-401-002", "Authentication required."),

    /**
     * Invalid JWT token.
     */
    INVALID_JWT("AUTH-401-003", "Invalid JWT token."),

    /**
     * JWT token has expired.
     */
    JWT_EXPIRED("AUTH-401-004", "JWT token has expired."),

    /**
     * Invalid OpenID Connect user.
     */
    INVALID_OIDC_USER("AUTH-401-005", "Invalid OpenID Connect user."),

    // =========================================================
    // User Errors
    // =========================================================

    /**
     * User already exists.
     */
    USER_ALREADY_EXISTS("USER-409-001", "User already exists."),

    /**
     * Requested user could not be found.
     */
    USER_NOT_FOUND("USER-404-001", "Requested user was not found."),

    // =========================================================
    // Item Errors
    // =========================================================

    /**
     * Requested item could not be found.
     */
    ITEM_NOT_FOUND("ITEM-404-001", "Requested item was not found."),

    /**
     * Requested item details could not be found.
     */
    ITEM_DETAILS_NOT_FOUND("ITEM-404-002", "Requested item details were not found."),

    /**
     * Requested review could not be found.
     */
    ITEM_REVIEW_NOT_FOUND("REVIEW-404-001", "Requested review was not found."),

    // =========================================================
    // Order Errors
    // =========================================================

    /**
     * Requested order could not be found.
     */
    ORDER_NOT_FOUND("ORDER-404-001", "Requested order was not found."),

    /**
     * Invalid operation attempted.
     */
    INVALID_OPERATION("SYSTEM-400-001", "Invalid operation."),

    // =========================================================
    // Role Errors
    // =========================================================

    /**
     * Requested role could not be found.
     */
    ROLE_NOT_FOUND("ROLE-404-001", "Requested role was not found."),

    // =========================================================
    // OAuth2 Registration Errors
    // =========================================================

    /**
     * The OAuth2 registration session is missing, invalid, or has expired.
     */
    OAUTH_SESSION_INVALID("AUTH-400-001", "OAuth registration session is invalid or has expired.");

    /**
     * Stable machine-readable error identifier.
     *
     * <p>
     * This value forms part of the public API contract and should never
     * change once released.
     * </p>
     */
    private final String code;

    /**
     * Default human-readable error message.
     *
     * <p>
     * Used when a more specific message is not provided.
     * </p>
     */
    private final String defaultMessage;

}