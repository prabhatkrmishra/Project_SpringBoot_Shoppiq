package com.pkmprojects.shoppiq.dto.admin.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk creation of product reviews by an administrator.
 *
 * <p>This record wraps a list of {@link AdminReviewItem} entries and is
 * submitted to the admin bulk review endpoint for creating multiple
 * product reviews in a single API call. It is primarily used for
 * test-data population during development and staging, enabling
 * administrators to populate the review system with sample data
 * at scale.</p>
 *
 * <p>Each element in the list undergoes cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that user
 * references, item references, and rating values meet their
 * respective constraints. Reviews created through this endpoint
 * are automatically set to PENDING moderation status at the service
 * layer and must be approved before appearing on product pages.
 * The list must not be empty.</p>
 *
 * @param reviews list of review creation requests, each specifying
 *                a target user, product, rating, and optional review
 *                text; must not be empty; each element is validated
 *                recursively via {@link AdminReviewItem}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkReviewRequest(
        /**
         * List of review creation requests. Must not be empty.
         */
        @NotEmpty(message = "At least one review is required.")
        List<@Valid AdminReviewItem> reviews
) {
}
