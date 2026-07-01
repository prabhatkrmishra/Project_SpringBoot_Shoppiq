package com.pkmprojects.shoppiq.dto.admin.response;

import com.pkmprojects.shoppiq.entity.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for recent activity feed on the admin dashboard.
 *
 * <p>
 * This DTO aggregates the most recent orders, payments, reviews,
 * and user registrations for the administrator dashboard activity feed.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Expose recent activity data to the admin API.</li>
 *     <li>Provide a unified view of recent platform activity.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Immutable through Java Records.</li>
 *     <li>Nested records for each activity type.</li>
 *     <li>Limited to the most recent 10 entries per category.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * Recent order data point.
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
     * Recent payment data point.
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
     * Recent review data point.
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
     * Recent user registration data point.
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