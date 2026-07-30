package com.pkmprojects.shoppiq.dto.admin.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for the recent activity feed on the admin dashboard.
 *
 * <p>This record aggregates the most recent platform activity across
 * four categories: orders, payments, reviews, and user registrations.
 * Each category is limited to the 10 most recent entries, providing
 * administrators with a quick overview of recent platform activity
 * without requiring navigation to individual management pages.</p>
 *
 * <p>The nested data records ({@link RecentOrderData},
 * {@link RecentPaymentData}, {@link RecentReviewData},
 * {@link RecentUserData}) are lightweight projections containing only
 * the fields needed for the dashboard feed display. They carry no
 * validation constraints and are never used as request bodies.</p>
 *
 * @param recentOrders   the 10 most recent orders placed on the platform,
 *                       ordered by placement timestamp descending
 * @param recentPayments the 10 most recent payment transactions processed,
 *                       ordered by creation timestamp descending
 * @param recentReviews  the 10 most recent product reviews submitted by
 *                       customers, ordered by creation timestamp descending
 * @param recentUsers    the 10 most recent user account registrations,
 *                       ordered by creation timestamp descending
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record RecentActivityResponse(

        /**
         * Most recent 10 orders.
         */
        List<RecentOrderData> recentOrders,

        /**
         * Most recent 10 payments.
         */
        List<RecentPaymentData> recentPayments,

        /**
         * Most recent 10 reviews.
         */
        List<RecentReviewData> recentReviews,

        /**
         * Most recent 10 user registrations.
         */
        List<RecentUserData> recentUsers

) {

    /**
     * Recent order data point for the dashboard activity feed.
     *
     * <p>Lightweight projection containing only the fields needed for
     * the activity feed display: order identifier, customer username,
     * status label, grand total, and placement timestamp.</p>
     *
     * @param orderId          unique identifier of the order
     * @param customerUsername username of the customer who placed the order
     * @param status           current order status as a string label
     * @param grandTotal       final amount the customer paid for this order
     * @param placedAt         timestamp when the order was placed
     */
    public record RecentOrderData(

            /**
             * Order identifier.
             */
            Long orderId,

            /**
             * Customer username.
             */
            String customerUsername,

            /**
             * Order status.
             */
            String status,

            /**
             * Order grand total.
             */
            BigDecimal grandTotal,

            /**
             * Order placement timestamp.
             */
            Instant placedAt
    ) {
    }

    /**
     * Recent payment data point for the dashboard activity feed.
     *
     * <p>Lightweight projection containing only the fields needed for
     * the activity feed display: payment identifier, reference code,
     * customer username, status label, amount, and creation timestamp.</p>
     *
     * @param paymentId        unique identifier of the payment record
     * @param paymentReference internal reference code for display and lookup
     * @param customerUsername username of the customer who made the payment
     * @param paymentStatus    current payment status as a string label
     * @param amount           monetary amount of the payment
     * @param createdAt        timestamp when the payment record was created
     */
    public record RecentPaymentData(

            /**
             * Payment identifier.
             */
            Long paymentId,

            /**
             * Payment reference.
             */
            String paymentReference,

            /**
             * Customer username.
             */
            String customerUsername,

            /**
             * Payment status.
             */
            String paymentStatus,

            /**
             * Payment amount.
             */
            BigDecimal amount,

            /**
             * Payment creation timestamp.
             */
            Instant createdAt
    ) {
    }

    /**
     * Recent review data point for the dashboard activity feed.
     *
     * <p>Lightweight projection containing only the fields needed for
     * the activity feed display: review identifier, product name,
     * reviewer username, rating, and creation timestamp.</p>
     *
     * @param reviewId         unique identifier of the review record
     * @param itemName         name of the product being reviewed
     * @param reviewerUsername username of the customer who submitted the review
     * @param rating           integer rating from 1 to 5 assigned by the reviewer
     * @param createdAt        timestamp when the review was first submitted
     */
    public record RecentReviewData(

            /**
             * Review identifier.
             */
            Long reviewId,

            /**
             * Product name.
             */
            String itemName,

            /**
             * Reviewer username.
             */
            String reviewerUsername,

            /**
             * Rating (1-5).
             */
            int rating,

            /**
             * Review creation timestamp.
             */
            Instant createdAt
    ) {
    }

    /**
     * Recent user registration data point for the dashboard activity feed.
     *
     * <p>Lightweight projection containing only the fields needed for
     * the activity feed display: user identifier, username, email, and
     * registration timestamp.</p>
     *
     * @param userId    unique identifier of the newly registered user
     * @param username  username chosen by the user during registration
     * @param email     email address associated with the new account
     * @param createdAt timestamp when the user account was created
     */
    public record RecentUserData(

            /**
             * User identifier.
             */
            Long userId,

            /**
             * Username.
             */
            String username,

            /**
             * User email.
             */
            String email,

            /**
             * Registration timestamp.
             */
            Instant createdAt
    ) {
    }
}
