package com.pkmprojects.shoppiq.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO used for bulk creation of {@link com.pkmprojects.shoppiq.entity.Item}.
 *
 * <p>
 * Wraps a list of {@link ItemRequest} to enable proper Bean Validation
 * on collection contents. Using this wrapper instead of a raw
 * {@code List<ItemRequest>} eliminates the Hibernate Validator warning
 * about {@code @Valid} directly on container types.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Accept client supplied product information in bulk.</li>
 *     <li>Perform request validation on the collection and its contents.</li>
 *     <li>Remain independent of persistence entities.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Marked as {@code final} through Java record semantics.</li>
 *     <li>{@code @NotEmpty} ensures the client supplies at least one item.</li>
 *     <li>{@code @Valid} triggers cascading validation on each
 *     {@link ItemRequest} in the list.</li>
 *     <li>Used exclusively for bulk creation; updates are handled
 *     individually.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public record BulkItemRequest(

        /*
          List of item creation requests.
         */
        @NotEmpty(message = "At least one item is required.")
        List<@Valid ItemRequest> items
) {
}