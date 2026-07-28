package com.pkmprojects.shoppiq.dto.cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Response payload for the authenticated user's full cart.
 *
 * <p>This Java record wraps a list of {@link CartItemResponse} together with
 * summary fields ({@code totalItems}, {@code subtotal}). The {@code subtotal}
 * is computed server-side from the line items, ensuring the frontend always
 * displays authoritative pricing.</p>
 *
 * <p><b>Frontend contract:</b> GET /api/cart returns this structure. The
 * frontend iterates {@code items} to render the cart UI and displays
 * {@code subtotal} as the order total before fees.</p>
 *
 * @param cartId     ID of the {@code Cart} record
 * @param totalItems total number of line items
 * @param subtotal   sum of all line totals
 * @param items      individual cart item responses
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record CartResponse(
        Long cartId,
        Integer totalItems,
        BigDecimal subtotal,
        List<CartItemResponse> items
) {}
