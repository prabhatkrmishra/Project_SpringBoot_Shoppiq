package com.pkmprojects.shoppiq.exception.order;

import com.pkmprojects.shoppiq.enums.OrderStatus;
import com.pkmprojects.shoppiq.exception.*;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for order-related exception classes.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DisplayName("Order Exception Tests")
class OrderExceptionTest {

    // ─── OrderNotFoundException ────────────────────────────────────────────

    @Test
    @DisplayName("OrderNotFoundException carries ORDER_NOT_FOUND code and 404 status")
    void orderNotFoundException_properties() {
        OrderNotFoundException ex = new OrderNotFoundException("Order '1' not found.");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getDetail()).contains("1");
    }

    // ─── OrderAccessDeniedException ────────────────────────────────────────

    @Test
    @DisplayName("OrderAccessDeniedException.forOrder — carries ORDER_ACCESS_DENIED code and 403 status")
    void orderAccessDeniedException_forOrder() {
        OrderAccessDeniedException ex = OrderAccessDeniedException.forOrder(42L);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ORDER_ACCESS_DENIED);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getDetail()).contains("42");
    }

    // ─── CartEmptyException ────────────────────────────────────────────────

    @Test
    @DisplayName("CartEmptyException carries CART_EMPTY code and 400 status")
    void cartEmptyException_properties() {
        CartEmptyException ex = new CartEmptyException();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CART_EMPTY);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getDetail()).isNotBlank();
    }

    // ─── OrderCannotBeCancelledException ──────────────────────────────────

    @Test
    @DisplayName("OrderCannotBeCancelledException carries ORDER_CANNOT_BE_CANCELLED and 400 status")
    void orderCannotBeCancelledException_properties() {
        OrderCannotBeCancelledException ex =
                new OrderCannotBeCancelledException(10L, OrderStatus.SHIPPED);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getDetail()).contains("10").contains("SHIPPED");
    }

    @Test
    @DisplayName("OrderCannotBeCancelledException — DELIVERED status")
    void orderCannotBeCancelledException_delivered() {
        OrderCannotBeCancelledException ex =
                new OrderCannotBeCancelledException(5L, OrderStatus.DELIVERED);

        assertThat(ex.getDetail()).contains("DELIVERED");
    }

    @Test
    @DisplayName("OrderCannotBeCancelledException — already CANCELLED")
    void orderCannotBeCancelledException_alreadyCancelled() {
        OrderCannotBeCancelledException ex =
                new OrderCannotBeCancelledException(7L, OrderStatus.CANCELLED);

        assertThat(ex.getDetail()).contains("CANCELLED");
    }
}