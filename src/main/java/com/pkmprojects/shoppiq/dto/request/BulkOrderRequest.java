package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO used for bulk creation of {@link com.pkmprojects.shoppiq.entity.Order orders}
 * by an admin user for test-data population.
 *
 * <p>
 * Wraps a list of {@link AdminOrderItem} to enable proper Bean Validation
 * on collection contents.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Accept admin-supplied order information in bulk.</li>
 *     <li>Perform request validation on the collection and its contents.</li>
 *     <li>Remain independent of persistence entities.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Marked as {@code final} through Java record semantics.</li>
 *     <li>{@code @NotEmpty} ensures the client supplies at least one order.</li>
 *     <li>{@code @Valid} triggers cascading validation on each
 *     {@link AdminOrderItem} in the list.</li>
 *     <li>Used exclusively for admin test-data bulk creation.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record BulkOrderRequest(
        @NotEmpty(message = "At least one order is required.")
        List<@Valid AdminOrderItem> orders
) {
}
