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
    // Category Errors
    // =========================================================

    /**
     * Category already exists.
     */
    CATEGORY_ALREADY_EXISTS("CATEGORY-409-001", "Category already exists."),

    /**
     * Requested category could not be found.
     */
    CATEGORY_NOT_FOUND("CATEGORY-404-001", "Requested category was not found."),

    // =========================================================
    // Item Errors
    // =========================================================

    /**
     * Requested item could not be found.
     */
    ITEM_NOT_FOUND("ITEM-404-001", "Requested item was not found."),

    /**
     * Requested item already exists.
     */
    ITEM_ALREADY_EXISTS("ITEM-409-001", "Requested item already exists."),

    /**
     * Stock adjustment would result in negative inventory.
     */
    ITEM_STOCK_NEGATIVE("ITEM-400-001", "Stock adjustment would result in negative inventory."),

    /**
     * Product is not yet published (DRAFT or REJECTED).
     */
    ITEM_NOT_PUBLISHED("ITEM-400-002", "Product is not yet published."),

    // =========================================================
    // ItemDetails Errors
    // =========================================================

    /**
     * Requested item details could not be found.
     */
    ITEM_DETAILS_NOT_FOUND("ITEM_DETAILS-404-002", "Requested item details were not found."),

    // =========================================================
    // ItemReview Errors
    // =========================================================

    /**
     * Requested review could not be found.
     */
    ITEM_REVIEW_NOT_FOUND("ITEM_REVIEW-404-001", "Requested item review was not found."),

    /**
     * Requested review already exists.
     */
    ITEM_REVIEW_ALREADY_EXISTS("ITEM_REVIEW-409-001", "Requested item review already exist."),

    /**
     * For attempt to modify or delete a review with no authorization.
     */
    ITEM_REVIEW_ACCESS_DENIED("ITEM_REVIEW-403-001", "You are not allowed to modify this review."),

    // =========================================================
    // Seller Errors
    // =========================================================

    /**
     * Requested seller could not be found.
     */
    SELLER_NOT_FOUND("SELLER-404-001", "Requested seller was not found."),

    /**
     * Seller already exists for this user.
     */
    SELLER_ALREADY_EXISTS("SELLER-409-001", "A seller profile already exists for this user."),

    /**
     * Seller is not yet verified.
     */
    SELLER_NOT_VERIFIED("SELLER-400-001", "Seller account is not verified."),

    /**
     * Seller is currently suspended.
     */
    SELLER_SUSPENDED("SELLER-400-002", "Seller account is suspended."),

    // =========================================================
    // Store Errors
    // =========================================================

    /**
     * Requested store could not be found.
     */
    STORE_NOT_FOUND("STORE-404-001", "Requested store was not found."),

    // =========================================================
    // Order Errors
    // =========================================================

    /**
     * Requested order could not be found.
     */
    ORDER_NOT_FOUND("ORDER-404-001", "Requested order was not found."),

    /**
     * Order does not belong to the current user.
     */
    ORDER_ACCESS_DENIED("ORDER-403-001", "You are not allowed to access this order."),

    /**
     * Cart is empty at checkout.
     */
    CART_EMPTY("CART-400-002", "Your cart is empty."),

    /**
     * Order cannot be cancelled in its current state.
     */
    ORDER_CANNOT_BE_CANCELLED("ORDER-400-001", "This order cannot be cancelled."),

    /**
     * Invalid order status transition.
     */
    ORDER_INVALID_STATUS_TRANSITION("ORDER-400-002", "Invalid order status transition."),

    // =========================================================
    // Role Errors
    // =========================================================

    /**
     * Requested role could not be found.
     */
    ROLE_NOT_FOUND("ROLE-404-001", "Requested role was not found."),

    // =========================================================
    // Cart Errors
    // =========================================================

    /**
     * Requested cart item could not be found.
     */
    CART_ITEM_NOT_FOUND("CART-404-001", "Requested cart item was not found."),

    /**
     * Cart item does not belong to the current user's cart.
     */
    CART_ITEM_ACCESS_DENIED("CART-403-001", "You are not allowed to access this cart item."),

    /**
     * Requested quantity exceeds available stock.
     */
    INSUFFICIENT_STOCK("CART-400-001", "Insufficient stock for the requested quantity."),

    // =========================================================
    // Address Errors
    // =========================================================

    /**
     * Requested address could not be found.
     */
    ADDRESS_NOT_FOUND("ADDRESS-404-001", "Requested address was not found."),

    /**
     * Address does not belong to the current user.
     */
    ADDRESS_ACCESS_DENIED("ADDRESS-403-001", "You are not allowed to access this address."),

    // =========================================================
    // Other Errors
    // =========================================================

    /**
     * Invalid operation attempted.
     */
    INVALID_OPERATION("SYSTEM-400-001", "Invalid operation."),

    // =========================================================
    // OAuth2 Registration Errors
    // =========================================================

    /**
     * The OAuth2 registration session is missing, invalid, or has expired.
     */
    OAUTH_SESSION_INVALID("AUTH-400-001", "OAuth registration session is invalid or has expired."),

    // =========================================================
    // Payment Errors
    // =========================================================

    /**
     * Requested payment could not be found.
     */
    PAYMENT_NOT_FOUND("PAYMENT-404-001", "Requested payment was not found."),

    /**
     * Payment does not belong to the current user.
     */
    PAYMENT_ACCESS_DENIED("PAYMENT-403-001", "You are not allowed to access this payment."),

    /**
     * A payment record already exists for this order.
     */
    PAYMENT_ALREADY_EXISTS("PAYMENT-409-001", "A payment already exists for this order."),

    /**
     * Payment cannot be processed in its current state.
     */
    PAYMENT_INVALID_STATE("PAYMENT-400-001", "Payment cannot be processed in its current state."),

    /**
     * Refund is not allowed for this payment.
     */
    PAYMENT_REFUND_NOT_ALLOWED("PAYMENT-400-002", "Refund is only allowed for paid payments."),

    /**
     * Payment cannot be cancelled.
     */
    PAYMENT_CANNOT_BE_CANCELLED("PAYMENT-400-003", "This payment cannot be cancelled."),

    /**
     * Transaction ID not found for verification.
     */
    PAYMENT_TRANSACTION_NOT_FOUND("PAYMENT-404-002", "No payment found for the given transaction ID."),

    /**
     * Communication with the external payment gateway failed.
     */
    PAYMENT_GATEWAY_ERROR("PAYMENT-502-001", "Communication with the payment gateway failed."),

    // =========================================================
    // Admin Errors
    // =========================================================

    /**
     * Admin cannot block their own account.
     */
    AUTH_BLOCK_SELF("ADMIN-403-001", "Administrators cannot disable their own account."),

    /**
     * Admin cannot unblock their own account.
     */
    AUTH_UNBLOCK_SELF("ADMIN-403-002", "Administrators cannot enable their own account.");

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