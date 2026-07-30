/**
 * Exceptions specific to the AI chat service.
 *
 * <p>Contains custom exception classes that represent AI-specific error
 * scenarios such as service unavailability, unsupported model requests,
 * access denial for conversation ownership, and general AI API failures.
 * Each exception maps to a specific HTTP status code and error code
 * for consistent error handling across the application.</p>
 *
 * <p>All exceptions extend {@code ShoppiqException} and integrate with
 * the application's global exception handler for structured error
 * responses.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.aiservice.exception;
