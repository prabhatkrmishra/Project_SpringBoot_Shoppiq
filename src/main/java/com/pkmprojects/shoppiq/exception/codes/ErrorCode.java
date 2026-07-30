package com.pkmprojects.shoppiq.exception.codes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum-based error code registry providing stable, machine-readable identifiers
 * for every application error.
 *
 * <p>Each error code follows the format {@code MODULE-HTTP_STATUS-SEQUENCE},
 * where MODULE identifies the functional area (AUTH, USER, ITEM, ORDER, etc.),
 * HTTP_STATUS is the numeric status code, and SEQUENCE is a zero-padded
 * sequence number within that module. This format makes error codes both
 * human-readable and machine-parseable, enabling clients to implement
 * automated error handling based on the module and status components.</p>
 *
 * <p>Error codes are the public API contract for error identification. Once
 * released, a code must never change or be removed, as external clients may
 * depend on it. New codes should be added at the end of each module section
 * to preserve backward compatibility. The {@link #defaultMessage} provides
 * a fallback human-readable message when no context-specific message is
 * available.</p>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.exception.base.ShoppiqException
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // =========================================================
    // Generic Errors
    // =========================================================

    /**
     * Unexpected server error thrown when an unhandled exception occurs.
     *
     * <p>This is the catch-all error code for any exception that is not
     * explicitly handled by a more specific handler. It returns HTTP 500
     * and a generic message to avoid leaking internal details.</p>
     */
    INTERNAL_SERVER_ERROR("SYSTEM-500-001", "An unexpected error occurred."),

    /**
     * Request validation failed due to missing or invalid fields.
     *
     * <p>Returned when Jakarta Bean Validation constraints are violated,
     * required request parameters are missing, or the request body cannot
     * be deserialized. The detail message includes specific field-level
     * error information.</p>
     */
    VALIDATION_FAILED("VALIDATION-400-001", "Request validation failed."),

    /**
     * Requested static resource or API endpoint could not be found.
     *
     * <p>Returned when a client requests a path that does not map to any
     * controller method or static resource. The handler checks the Accept
     * header to return JSON for API clients or forward to the HTML error
     * page for browser requests.</p>
     */
    RESOURCE_NOT_FOUND("SYSTEM-404-001", "Requested resource was not found."),

    /**
     * The authenticated user does not have permission to perform the requested action.
     *
     * <p>Returned when a user is authenticated but lacks the required role
     * or ownership to access a resource. This is distinct from
     * {@link #UNAUTHORIZED}, which indicates that no authentication was
     * provided at all.</p>
     */
    ACCESS_DENIED("AUTH-403-001", "Access denied."),

    /**
     * Database constraint violation such as a unique key, NOT NULL, or foreign key violation.
     *
     * <p>Returned when a JPA persistence operation fails due to a database
     * constraint. The actual SQL-level detail is logged at WARN level for
     * diagnostics, but the client receives a generic message to avoid
     * leaking schema details.</p>
     */
    DATA_INTEGRITY_VIOLATION("SYSTEM-409-001", "The request conflicts with existing data."),

    // =========================================================
    // Authentication Errors
    // =========================================================

    /**
     * Authentication required but not provided.
     *
     * <p>Returned when a request targets a protected endpoint without
     * a valid JWT token or session. The client should authenticate
     * and retry the request with valid credentials.</p>
     */
    UNAUTHORIZED("AUTH-401-001", "Authentication required."),

    /**
     * Invalid credentials supplied during login.
     *
     * <p>Returned when the username or password does not match any
     * record in the database. The message is intentionally vague to
     * prevent username enumeration attacks.</p>
     */
    INVALID_CREDENTIALS("AUTH-401-002", "Invalid credentials supplied."),

    /**
     * The JWT token is malformed, has an invalid signature, or is otherwise unparseable.
     *
     * <p>Returned when the JWT filter encounters a token that cannot
     * be parsed or verified. The client should obtain a new token
     * through the login endpoint.</p>
     */
    INVALID_JWT("AUTH-401-003", "Invalid JWT token."),

    /**
     * The JWT token has passed its expiration date.
     *
     * <p>Returned when a validly signed token is presented after its
     * {@code exp} claim has elapsed. The client should use the
     * refresh token endpoint to obtain a new access token.</p>
     */
    JWT_EXPIRED("AUTH-401-004", "JWT token has expired."),

    /**
     * The OpenID Connect user could not be mapped to a local user.
     *
     * <p>Returned during the OAuth2 login flow when the OIDC claims
     * cannot be processed or the user's email is missing from the
     * token. The user may need to complete the registration flow.</p>
     */
    INVALID_OIDC_USER("AUTH-401-005", "Invalid OpenID Connect user."),

    // =========================================================
    // User Errors
    // =========================================================

    /**
     * A user with the same email or username already exists.
     *
     * <p>Returned during registration when the submitted email or
     * username conflicts with an existing record. The client should
     * use a different identifier or proceed to the login flow.</p>
     */
    USER_ALREADY_EXISTS("USER-409-001", "User already exists."),

    /**
     * Requested user could not be found by ID or email.
     *
     * <p>Returned when a service method attempts to load a user that
     * does not exist in the database. The client should verify the
     * user identifier and retry.</p>
     */
    USER_NOT_FOUND("USER-404-001", "Requested user was not found."),

    /**
     * The supplied current password does not match the stored hash.
     *
     * <p>Returned during password-change operations when the user
     * provides an incorrect current password. This is a security
     * measure to prevent unauthorized password changes.</p>
     */
    CURRENT_PASSWORD_INCORRECT("USER-400-001", "Current password is incorrect."),

    // =========================================================
    // Category Errors
    // =========================================================

    /**
     * A category with the same name already exists.
     *
     * <p>Returned during category creation when the submitted name
     * conflicts with an existing category. Category names are unique
     * within the system to prevent confusion in the product catalog.</p>
     */
    CATEGORY_ALREADY_EXISTS("CATEGORY-409-001", "Category already exists."),

    /**
     * Requested category could not be found by ID or slug.
     *
     * <p>Returned when a service method attempts to load a category
     * that does not exist. This may indicate a stale URL or a
     * category that has been deleted.</p>
     */
    CATEGORY_NOT_FOUND("CATEGORY-404-001", "Requested category was not found."),

    // =========================================================
    // Item Errors
    // =========================================================

    /**
     * Requested item (product) could not be found by ID or slug.
     *
     * <p>Returned when a service method attempts to load a product
     * that does not exist. The client should verify the item
     * identifier and retry.</p>
     */
    ITEM_NOT_FOUND("ITEM-404-001", "Requested item was not found."),

    /**
     * An item with the same name or slug already exists.
     *
     * <p>Returned during item creation when the submitted name or
     * generated slug conflicts with an existing item. Slugs are
     * unique to ensure SEO-friendly URLs.</p>
     */
    ITEM_ALREADY_EXISTS("ITEM-409-001", "Requested item already exists."),

    /**
     * A stock adjustment would result in negative inventory.
     *
     * <p>Returned when an admin or seller attempts to reduce stock
     * below zero. The system prevents negative inventory to maintain
     * data integrity and accurate stock counts.</p>
     */
    ITEM_STOCK_NEGATIVE("ITEM-400-001", "Stock adjustment would result in negative inventory."),

    /**
     * Stock was modified by another transaction during checkout.
     *
     * <p>Returned when an optimistic locking conflict is detected
     * during the checkout process. Another customer or process has
     * modified the stock since it was last read. The client should
     * refresh the cart and retry the checkout.</p>
     */
    ITEM_STOCK_CONFLICT("ITEM-409-002", "Stock was modified by another customer. Please refresh and try again."),

    /**
     * The product is not yet published (still in DRAFT or REJECTED state).
     *
     * <p>Returned when a customer attempts to purchase or interact
     * with a product that has not been approved for sale. Only
     * APPROVED products are visible in the catalog.</p>
     */
    ITEM_NOT_PUBLISHED("ITEM-400-002", "Product is not yet published."),

    /**
     * The product is already on sale and cannot be discounted again.
     *
     * <p>Returned when an admin attempts to apply a sale price to
     * a product that is already discounted. Only one sale can be
     * active at a time per product.</p>
     */
    ITEM_ALREADY_ON_SALE("ITEM-400-003", "Product is already on sale."),

    // =========================================================
    // ItemDetails Errors
    // =========================================================

    /**
     * Requested item details (specifications, descriptions) could not be found.
     *
     * <p>Returned when attempting to load detailed product information
     * that does not exist. Item details are created separately from
     * the item itself and may not always be present.</p>
     */
    ITEM_DETAILS_NOT_FOUND("ITEM_DETAILS-404-002", "Requested item details were not found."),

    // =========================================================
    // ItemReview Errors
    // =========================================================

    /**
     * Requested item review could not be found.
     *
     * <p>Returned when attempting to load, update, or delete a review
     * that does not exist. The review may have been removed by the
     * author or by an administrator.</p>
     */
    ITEM_REVIEW_NOT_FOUND("ITEM_REVIEW-404-001", "Requested item review was not found."),

    /**
     * The user has already submitted a review for this item.
     *
     * <p>Returned during review creation when the authenticated user
     * already has a review for the specified item. Only one review
     * per user per item is allowed.</p>
     */
    ITEM_REVIEW_ALREADY_EXISTS("ITEM_REVIEW-409-001", "Requested item review already exists."),

    /**
     * The user is not authorized to modify or delete this review.
     *
     * <p>Returned when a user attempts to update or delete a review
     * that belongs to another user. Only the review author or an
     * administrator can modify a review.</p>
     */
    ITEM_REVIEW_ACCESS_DENIED("ITEM_REVIEW-403-001", "You are not allowed to modify this review."),

    // =========================================================
    // Seller Errors
    // =========================================================

    /**
     * Requested seller profile could not be found.
     *
     * <p>Returned when a service method attempts to load a seller
     * profile that does not exist. The seller may not have completed
     * the registration flow.</p>
     */
    SELLER_NOT_FOUND("SELLER-404-001", "Requested seller was not found."),

    /**
     * A seller profile already exists for this user.
     *
     * <p>Returned during seller registration when the authenticated
     * user already has an associated seller profile. Each user can
     * have at most one seller profile.</p>
     */
    SELLER_ALREADY_EXISTS("SELLER-409-001", "A seller profile already exists for this user."),

    /**
     * The seller account is not yet verified by an administrator.
     *
     * <p>Returned when a seller attempts to perform actions that
     * require a verified account. Sellers must be approved by an
     * admin before they can list products or receive orders.</p>
     */
    SELLER_NOT_VERIFIED("SELLER-400-001", "Seller account is not verified."),

    /**
     * The seller account has been suspended by an administrator.
     *
     * <p>Returned when a suspended seller attempts to access
     * restricted functionality. Suspended sellers cannot list
     * products or receive new orders until reactivated.</p>
     */
    SELLER_SUSPENDED("SELLER-400-002", "Seller account is suspended."),

    // =========================================================
    // Store Errors
    // =========================================================

    /**
     * Requested store could not be found.
     *
     * <p>Returned when a service method attempts to load a store
     * that does not exist. Each seller has one associated store
     * that is created during the seller registration flow.</p>
     */
    STORE_NOT_FOUND("STORE-404-001", "Requested store was not found."),

    // =========================================================
    // Order Errors
    // =========================================================

    /**
     * Requested order could not be found.
     *
     * <p>Returned when a service method attempts to load an order
     * that does not exist. The order ID may be incorrect or the
     * order may have been soft-deleted.</p>
     */
    ORDER_NOT_FOUND("ORDER-404-001", "Requested order was not found."),

    /**
     * The order does not belong to the authenticated user.
     *
     * <p>Returned when a customer attempts to access an order that
     * was placed by another user. Orders are private and can only
     * be viewed by the purchaser or an administrator.</p>
     */
    ORDER_ACCESS_DENIED("ORDER-403-001", "You are not allowed to access this order."),

    /**
     * The customer's cart is empty when attempting to place an order.
     *
     * <p>Returned at checkout when the cart contains no items. The
     * customer must add items to the cart before proceeding to
     * checkout.</p>
     */
    CART_EMPTY("CART-400-002", "Your cart is empty."),

    /**
     * The order cannot be cancelled in its current status.
     *
     * <p>Returned when a customer or admin attempts to cancel an
     * order that is already shipped, delivered, or in a
     * non-cancellable state.</p>
     */
    ORDER_CANNOT_BE_CANCELLED("ORDER-400-001", "This order cannot be cancelled."),

    /**
     * The requested order status transition is not allowed.
     *
     * <p>Returned when an admin attempts to move an order to a
     * status that is not reachable from the current status. The
     * order status machine defines valid transitions.</p>
     */
    ORDER_INVALID_STATUS_TRANSITION("ORDER-400-002", "Invalid order status transition."),

    // =========================================================
    // Role Errors
    // =========================================================

    /**
     * Requested role could not be found.
     *
     * <p>Returned when a service method attempts to load a role
     * that does not exist in the database. Roles are seeded at
     * application startup and should always be present.</p>
     */
    ROLE_NOT_FOUND("ROLE-404-001", "Requested role was not found."),

    // =========================================================
    // Cart Errors
    // =========================================================

    /**
     * Requested cart item could not be found.
     *
     * <p>Returned when a service method attempts to load a cart
     * item that does not exist. The item may have been removed
     * from the cart by the user or by a timeout.</p>
     */
    CART_ITEM_NOT_FOUND("CART-404-001", "Requested cart item was not found."),

    /**
     * The cart item does not belong to the authenticated user's cart.
     *
     * <p>Returned when a user attempts to modify a cart item that
     * belongs to another user's cart. Each user has an isolated
     * cart that is not accessible to others.</p>
     */
    CART_ITEM_ACCESS_DENIED("CART-403-001", "You are not allowed to access this cart item."),

    /**
     * The requested quantity exceeds available stock.
     *
     * <p>Returned when a customer attempts to add more units of an
     * item to their cart than are currently in stock. The system
     * prevents over-commitment of inventory.</p>
     */
    INSUFFICIENT_STOCK("CART-400-001", "Insufficient stock for the requested quantity."),

    // =========================================================
    // Address Errors
    // =========================================================

    /**
     * Requested address could not be found.
     *
     * <p>Returned when a service method attempts to load an address
     * that does not exist. The address may have been deleted by
     * the user.</p>
     */
    ADDRESS_NOT_FOUND("ADDRESS-404-001", "Requested address was not found."),

    /**
     * The address does not belong to the authenticated user.
     *
     * <p>Returned when a user attempts to access or modify an address
     * that belongs to another user. Addresses are private and can
     * only be managed by the owner.</p>
     */
    ADDRESS_ACCESS_DENIED("ADDRESS-403-001", "You are not allowed to access this address."),

    // =========================================================
    // Other Errors
    // =========================================================

    /**
     * An invalid operation was attempted.
     *
     * <p>Generic error for operations that violate business rules
     * but do not fit into a more specific error category. The detail
     * message should explain what operation was attempted and why
     * it is not allowed.</p>
     */
    INVALID_OPERATION("SYSTEM-400-001", "Invalid operation."),

    // =========================================================
    // OAuth2 Registration Errors
    // =========================================================

    /**
     * The OAuth2 registration session is missing, invalid, or has expired.
     *
     * <p>Returned during the OAuth2 registration completion flow when
     * the session cookie is absent or no longer valid. The user must
     * re-initiate the Google login flow to create a new session.</p>
     */
    OAUTH_SESSION_INVALID("AUTH-400-001", "OAuth registration session is invalid or has expired."),

    // =========================================================
    // Payment Errors
    // =========================================================

    /**
     * Requested payment record could not be found.
     *
     * <p>Returned when a service method attempts to load a payment
     * that does not exist. The payment ID may be incorrect or the
     * payment may not have been initiated yet.</p>
     */
    PAYMENT_NOT_FOUND("PAYMENT-404-001", "Requested payment was not found."),

    /**
     * The payment does not belong to the authenticated user.
     *
     * <p>Returned when a customer attempts to access a payment
     * record that belongs to another user. Payment records are
     * private and restricted to the payer and administrators.</p>
     */
    PAYMENT_ACCESS_DENIED("PAYMENT-403-001", "You are not allowed to access this payment."),

    /**
     * A payment record already exists for this order.
     *
     * <p>Returned when attempting to create a duplicate payment for
     * an order that already has an associated payment. Each order
     * can have at most one payment record.</p>
     */
    PAYMENT_ALREADY_EXISTS("PAYMENT-409-001", "A payment already exists for this order."),

    /**
     * The payment cannot be processed in its current state.
     *
     * <p>Returned when an operation is attempted on a payment that
     * is not in the correct state. For example, attempting to
     * capture a payment that is still in PENDING status.</p>
     */
    PAYMENT_INVALID_STATE("PAYMENT-400-001", "Payment cannot be processed in its current state."),

    /**
     * A refund is not allowed for this payment.
     *
     * <p>Returned when attempting to refund a payment that is not
     * in PAID status. Only fully paid payments can be refunded.</p>
     */
    PAYMENT_REFUND_NOT_ALLOWED("PAYMENT-400-002", "Refund is only allowed for paid payments."),

    /**
     * The payment cannot be cancelled in its current state.
     *
     * <p>Returned when attempting to cancel a payment that is
     * already completed, refunded, or in a non-cancellable state.</p>
     */
    PAYMENT_CANNOT_BE_CANCELLED("PAYMENT-400-003", "This payment cannot be cancelled."),

    /**
     * No payment record matches the given transaction ID.
     *
     * <p>Returned during payment verification when the transaction
     * ID from the gateway callback does not match any stored
     * payment record.</p>
     */
    PAYMENT_TRANSACTION_NOT_FOUND("PAYMENT-404-002", "No payment found for the given transaction ID."),

    /**
     * Communication with the external payment gateway failed.
     *
     * <p>Returned when the application cannot reach the payment
     * provider's API, or the provider returns an unexpected error.
     * The client should retry the payment or choose a different
     * payment method.</p>
     */
    PAYMENT_GATEWAY_ERROR("PAYMENT-502-001", "Communication with the payment gateway failed."),

    // =========================================================
    // Promo Code Errors
    // =========================================================

    /**
     * Requested promo code could not be found.
     *
     * <p>Returned when a customer applies a promo code that does
     * not exist in the system. The code may be misspelled or
     * expired and removed from the database.</p>
     */
    PROMO_CODE_NOT_FOUND("PROMO-404-001", "Requested promo code was not found."),

    /**
     * A promo code with the same code value already exists.
     *
     * <p>Returned during promo code creation when the submitted
     * code value conflicts with an existing record. Promo codes
     * must be unique across the system.</p>
     */
    PROMO_CODE_ALREADY_EXISTS("PROMO-409-001", "A promo code with this code already exists."),

    /**
     * The promo code has passed its expiration date.
     *
     * <p>Returned when a customer applies a promo code that is
     * past its valid end date. The code can no longer be used
     * for any orders.</p>
     */
    PROMO_CODE_EXPIRED("PROMO-400-001", "Promo code has expired."),

    /**
     * The promo code is not yet valid (before its start date).
     *
     * <p>Returned when a customer applies a promo code before
     * its valid start date. The code will become active on the
     * specified date.</p>
     */
    PROMO_CODE_NOT_YET_VALID("PROMO-400-002", "Promo code is not yet valid."),

    /**
     * The promo code is currently inactive.
     *
     * <p>Returned when a customer applies a promo code that has
     * been deactivated by an administrator. The code exists but
     * is not currently usable.</p>
     */
    PROMO_CODE_INACTIVE("PROMO-400-003", "Promo code is currently inactive."),

    /**
     * The promo code's global usage limit has been reached.
     *
     * <p>Returned when the total number of times this promo code
     * has been used across all customers equals the configured
     * maximum. No further uses are allowed.</p>
     */
    PROMO_CODE_USAGE_LIMIT_EXCEEDED("PROMO-400-004", "Promo code usage limit has been reached."),

    /**
     * The customer has exceeded their per-user usage limit for this promo code.
     *
     * <p>Returned when an individual customer has used this promo
     * code the maximum number of times allowed per user. Other
     * customers may still be able to use it.</p>
     */
    PROMO_CODE_USER_USAGE_LIMIT_EXCEEDED("PROMO-400-005", "You have reached the usage limit for this promo code."),

    /**
     * The order subtotal does not meet the promo code's minimum amount.
     *
     * <p>Returned when the customer's cart subtotal is below the
     * minimum order amount required by the promo code. The customer
     * must add more items to qualify.</p>
     */
    PROMO_CODE_MIN_ORDER_AMOUNT_NOT_MET("PROMO-400-006", "Order subtotal does not meet the promo code minimum."),

    /**
     * The cart does not meet the promo code's type or quantity constraint.
     *
     * <p>Returned when the promo code requires specific item types
     * or minimum quantities that the customer's cart does not
     * satisfy. The detail message explains the specific constraint.</p>
     */
    PROMO_CODE_CART_CONSTRAINT("PROMO-400-007", "Cart does not meet the promo code type or quantity requirement."),

    // =========================================================
    // Banner Errors
    // =========================================================

    /**
     * Requested homepage banner could not be found.
     *
     * <p>Returned when a service method attempts to load a banner
     * that does not exist. Banners are used on the homepage to
     * promote sales and featured products.</p>
     */
    BANNER_NOT_FOUND("BANNER-404-001", "Requested banner was not found."),

    // =========================================================
    // Email Errors
    // =========================================================

    /**
     * The email failed to send through the configured provider.
     *
     * <p>Returned when the email service encounters an error while
     * attempting to deliver a message. This may be due to SMTP
     * connectivity issues, invalid recipient addresses, or provider
     * rate limits. The actual error is logged for diagnostics.</p>
     */
    EMAIL_SEND_FAILED("EMAIL-502-001", "Failed to send email."),

    // =========================================================
    // Verification Code Errors
    // =========================================================

    /**
     * The submitted verification code is invalid.
     *
     * <p>Returned when the code entered by the user does not match
     * any active verification code in the database. The user should
     * double-check the code and try again.</p>
     */
    VERIFICATION_CODE_INVALID("VERIFY-400-001", "Invalid verification code."),

    /**
     * The verification code has expired.
     *
     * <p>Returned when the code's TTL has elapsed. Expired codes
     * are rejected immediately. The user must request a new code
     * through the resend flow.</p>
     */
    VERIFICATION_CODE_EXPIRED("VERIFY-400-002", "Verification code has expired."),

    /**
     * The maximum number of verification attempts has been exceeded.
     *
     * <p>Returned when the user has entered incorrect codes too
     * many times. This is a brute-force protection mechanism. The
     * user must request a new code after a cooldown period.</p>
     */
    VERIFICATION_CODE_MAX_ATTEMPTS("VERIFY-429-001", "Maximum verification attempts exceeded."),

    // =========================================================
    // Rate Limit Errors
    // =========================================================

    /**
     * The client has exceeded the allowed request rate.
     *
     * <p>Returned by the rate limit filter when a client makes too
     * many requests within the configured time window. The client
     * should wait before retrying. The response includes a
     * Retry-After header indicating when to retry.</p>
     */
    RATE_LIMIT_EXCEEDED("AUTH-429-001", "Too many requests. Please try again later."),

    // =========================================================
    // Contact Message Errors
    // =========================================================

    /**
     * Requested contact message could not be found.
     *
     * <p>Returned when an admin attempts to load a contact message
     * that does not exist. The message may have been deleted by
     * another administrator.</p>
     */
    CONTACT_MESSAGE_NOT_FOUND("CONTACT_MESSAGE-404-001", "Requested contact message was not found."),

    // =========================================================
    // Admin Errors
    // =========================================================

    /**
     * An administrator attempted to block their own account.
     *
     * <p>Returned when an admin tries to disable their own account.
     * This is prevented to avoid accidentally locking themselves
     * out of the system. Another admin must perform the action.</p>
     */
    AUTH_BLOCK_SELF("ADMIN-403-001", "Administrators cannot disable their own account."),

    /**
     * An administrator attempted to unblock their own account.
     *
     * <p>Returned when an admin tries to enable their own account.
     * This operation is redundant if the admin is already active
     * and is blocked to prevent confusion in the audit log.</p>
     */
    AUTH_UNBLOCK_SELF("ADMIN-403-002", "Administrators cannot enable their own account."),

    // =========================================================
    // AI Chat Errors
    // =========================================================

    /**
     * The requested AI conversation could not be found.
     *
     * <p>Returned when a user attempts to load a conversation that
     * does not exist. The conversation may have been deleted or
     * the ID may be incorrect.</p>
     */
    AI_CONVERSATION_NOT_FOUND("AI_CHAT-404-001", "AI conversation not found."),

    /**
     * An error occurred while communicating with the AI assistant.
     *
     * <p>Returned when the AI service encounters an internal error.
     * The client should retry the request or contact support if
     * the issue persists.</p>
     */
    AI_API_ERROR("AI_CHAT-500-001", "An error occurred with the AI assistant."),

    /**
     * The client has exceeded the AI request rate limit.
     *
     * <p>Returned when too many AI requests are made within the
     * configured time window. The client should wait before
     * sending the next message.</p>
     */
    AI_RATE_LIMITED("AI_CHAT-429-001", "Too many AI requests. Please try again later."),

    /**
     * The AI assistant took too long to respond.
     *
     * <p>Returned when the AI service does not respond within the
     * configured timeout. The client should retry the request,
     * possibly with a shorter message.</p>
     */
    AI_TIMEOUT("AI_CHAT-504-001", "AI assistant timed out. Please try again."),

    /**
     * The AI conversation has been resolved and is no longer active.
     *
     * <p>Returned when a user attempts to send a message to a
     * conversation that has been marked as resolved. The user
     * must start a new conversation.</p>
     */
    AI_CONVERSATION_RESOLVED("AI_CHAT-410-001", "This conversation has been resolved."),

    /**
     * The user does not have access to the requested AI conversation.
     *
     * <p>Returned when a user attempts to access a conversation
     * that belongs to another user. Conversations are private and
     * restricted to their creator.</p>
     */
    AI_ACCESS_DENIED("AI_CHAT-403-001", "You do not have access to this conversation."),

    /**
     * The requested AI model is not supported or not configured.
     *
     * <p>Returned when a user selects an AI model that is not
     * available in the current deployment. The client should
     * choose a different model.</p>
     */
    AI_MODEL_NOT_SUPPORTED("AI_CHAT-400-001", "The requested AI model is not supported."),

    /**
     * The AI service is currently unavailable.
     *
     * <p>Returned when the AI backend is down or unreachable. The
     * client should retry after a delay. This is typically a
     * transient error.</p>
     */
    AI_SERVICE_UNAVAILABLE("AI_CHAT-503-001", "AI service is not available. Please try again later."),

    /**
     * The database connection or commit has failed.
     *
     * <p>Returned when a database operation fails due to a connection
     * issue, a binary-log write failure, or a server-side abort. The
     * client should retry after a delay. This is typically a
     * transient infrastructure error, often caused by disk-space
     * exhaustion or a MySQL server restart.</p>
     */
    DATABASE_UNAVAILABLE("DB-503-001", "Database is temporarily unavailable. Please try again later."),

    // =========================================================
    // Infrastructure Errors
    // =========================================================

    /**
     * No email provider is configured for the requested email type.
     *
     * <p>Returned when the application attempts to send an email but
     * no provider (SMTP or Console) is registered for the requested
     * type. The administrator must configure an email provider in
     * the application properties.</p>
     */
    EMAIL_PROVIDER_NOT_FOUND("EMAIL-404-002", "Email provider not found."),

    /**
     * No payment gateway is configured for the requested payment method.
     *
     * <p>Returned when a customer selects a payment method that has
     * no enabled gateway. The administrator must configure and enable
     * the corresponding payment gateway.</p>
     */
    PAYMENT_GATEWAY_NOT_FOUND("PAYMENT-404-003", "Payment gateway not found."),

    /**
     * The payment gateway configuration is invalid or incomplete.
     *
     * <p>Returned when a payment gateway's configuration is missing
     * required fields or has invalid values. The administrator must
     * review and correct the gateway configuration.</p>
     */
    INVALID_PAYMENT_GATEWAY_CONFIG("PAYMENT-500-002", "Invalid payment gateway configuration."),

    /**
     * Failed to generate a unique slug after maximum attempts.
     *
     * <p>Returned when the slug generation algorithm cannot produce
     * a unique value after exhausting all retry attempts. This is
     * an extremely rare error that indicates a near-full slug
     * namespace for the given entity type.</p>
     */
    SLUG_GENERATION_FAILED("SYSTEM-500-002", "Failed to generate unique slug."),

    /**
     * The requested feature has not been implemented yet.
     *
     * <p>Returned when a user attempts to access functionality that
     * is planned but not yet available. This is a permanent error
     * that will be resolved in a future release.</p>
     */
    FEATURE_NOT_IMPLEMENTED("SYSTEM-501-001", "This feature is not yet implemented.");

    /**
     * Stable machine-readable error identifier.
     *
     * <p>This value forms part of the public API contract and should never
     * change once released. Clients may implement automated error handling
     * based on this code. The format is {@code MODULE-HTTP_STATUS-SEQUENCE}.</p>
     */
    private final String code;

    /**
     * Default human-readable error message.
     *
     * <p>Used when a more specific message is not provided by the exception
     * site. This message is included in the Problem Detail response's
     * {@code detail} field and should be clear enough for end users to
     * understand without technical knowledge.</p>
     */
    private final String defaultMessage;

}
