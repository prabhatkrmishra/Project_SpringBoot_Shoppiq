/**
 * Business exception hierarchy for common application-level error scenarios.
 *
 * <p>This package contains abstract base exceptions and concrete exception
 * classes that represent common business rule violations. Each base class
 * maps to a specific HTTP status code: {@link ResourceNotFoundException}
 * (404), {@link DuplicateResourceException} (409),
 * {@link InvalidOperationException} (400), and
 * {@link UnauthorizedOperationException} (403). Concrete subclasses
 * provide domain-specific error messages and error codes.</p>
 *
 * <p>These exceptions are thrown by service-layer methods when business
 * rules are violated. The global exception handler catches them and
 * converts them into RFC 9457 Problem Detail responses with the
 * appropriate HTTP status and error code.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.exception.business;
