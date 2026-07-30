/**
 * Administrator-specific exception hierarchy.
 *
 * <p>This package contains exceptions for administrative operations that
 * have special constraints. Currently, it handles the case where an
 * administrator attempts to block or unblock their own account, which
 * is prevented to avoid accidental lockouts. The exception uses HTTP 403
 * Forbidden status.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.exception.admin;
