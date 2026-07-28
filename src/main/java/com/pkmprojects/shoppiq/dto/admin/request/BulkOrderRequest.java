package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.entity.order.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Request DTO used for bulk creation of {@link Order orders}
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
 *     <li>This pattern is identical for all bulk DTOs — a single-record wrapper
 *     with a validated list — providing a consistent API experience.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkOrderRequest(
        @NotEmpty(message = "At least one order is required.")
        List<@Valid AdminOrderItem> orders
) {
}
