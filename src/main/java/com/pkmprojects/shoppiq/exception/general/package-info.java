/**
 * Domain-specific exception hierarchy organized by business subdomain.
 *
 * <p>This package contains concrete exception classes for each business
 * area: address, aiservice, banner, cart, category, contact, email, inventory,
 * item, order, payment, promo, review, seller, store, user, and verification.
 * Each sub-package groups exceptions that share a common domain context,
 * making it easy to find and manage errors for a specific feature area.</p>
 *
 * <p>All exceptions in this hierarchy extend the base exceptions from
 * {@link com.pkmprojects.shoppiq.exception.business} and use the error
 * codes defined in {@link com.pkmprojects.shoppiq.exception.codes.ErrorCode}.
 * The global exception handler catches these exceptions and converts them
 * into RFC 9457 Problem Detail responses.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.exception.general;
