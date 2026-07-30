/**
 * Exception hierarchy and error handling infrastructure for the Shoppiq application.
 *
 * <p>This package contains the complete exception management layer, including
 * the base exception class, error code registry, RFC 9457 Problem Detail
 * factory, validation error formatter, and global exception handler. Every
 * application error is converted into a structured Problem Detail response
 * that includes a machine-readable error code, human-readable detail message,
 * HTTP status, timestamp, and request URI.</p>
 *
 * <p>The exception hierarchy is organized by domain concern: base exceptions
 * define the contract, business exceptions represent application-level errors,
 * auth exceptions cover authentication and authorization failures, admin
 * exceptions handle administrator-specific constraints, and general
 * sub-packages group entity-specific errors (cart, item, order, payment,
 * etc.). The {@link com.pkmprojects.shoppiq.exception.handler.GlobalExceptionHandler}
 * acts as the single catch-all, mapping each exception type to the appropriate
 * HTTP response.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.exception;
