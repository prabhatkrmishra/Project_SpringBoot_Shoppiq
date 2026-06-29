package com.pkmprojects.shoppiq.exception.cart;

import com.pkmprojects.shoppiq.exception.CartItemAccessDeniedException;
import com.pkmprojects.shoppiq.exception.CartItemNotFoundException;
import com.pkmprojects.shoppiq.exception.InsufficientStockException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for cart-domain exception classes.
 *
 * <p>
 * Verifies that each exception carries the correct {@link ErrorCode},
 * HTTP status, and produces a meaningful detail message.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DisplayName("Cart Exception Tests")
class CartExceptionTest {

    // ---------------------------------------------------------------
    // CartItemNotFoundException
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("CartItemNotFoundException")
    class CartItemNotFoundExceptionTests {

        @Test
        @DisplayName("id() factory sets CART_ITEM_NOT_FOUND error code")
        void id_setsCorrectErrorCode() {
            CartItemNotFoundException ex = CartItemNotFoundException.id(42L);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("id() factory sets HTTP 404 status")
        void id_setsHttp404() {
            CartItemNotFoundException ex = CartItemNotFoundException.id(42L);
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("id() detail message contains the supplied ID")
        void id_detailContainsId() {
            CartItemNotFoundException ex = CartItemNotFoundException.id(99L);
            assertThat(ex.getDetail()).contains("99");
        }

        @Test
        @DisplayName("id() is a RuntimeException")
        void id_isRuntimeException() {
            assertThat(CartItemNotFoundException.id(1L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ---------------------------------------------------------------
    // CartItemAccessDeniedException
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("CartItemAccessDeniedException")
    class CartItemAccessDeniedExceptionTests {

        @Test
        @DisplayName("forItem() factory sets CART_ITEM_ACCESS_DENIED error code")
        void forItem_setsCorrectErrorCode() {
            CartItemAccessDeniedException ex = CartItemAccessDeniedException.forItem(5L);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_ACCESS_DENIED);
        }

        @Test
        @DisplayName("forItem() factory sets HTTP 403 status")
        void forItem_setsHttp403() {
            CartItemAccessDeniedException ex = CartItemAccessDeniedException.forItem(5L);
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("forItem() detail message contains the supplied cart item ID")
        void forItem_detailContainsCartItemId() {
            CartItemAccessDeniedException ex = CartItemAccessDeniedException.forItem(77L);
            assertThat(ex.getDetail()).contains("77");
        }
    }

    // ---------------------------------------------------------------
    // InsufficientStockException
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("InsufficientStockException")
    class InsufficientStockExceptionTests {

        @Test
        @DisplayName("forItem() factory sets INSUFFICIENT_STOCK error code")
        void forItem_setsCorrectErrorCode() {
            InsufficientStockException ex = InsufficientStockException.forItem("SKU-001", 10, 3);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
        }

        @Test
        @DisplayName("forItem() factory sets HTTP 400 status")
        void forItem_setsHttp400() {
            InsufficientStockException ex = InsufficientStockException.forItem("SKU-001", 10, 3);
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("forItem() detail message contains SKU, requested and available quantities")
        void forItem_detailContainsAllRelevantInfo() {
            InsufficientStockException ex =
                    InsufficientStockException.forItem("SKU-XYZ", 25, 5);
            assertThat(ex.getDetail())
                    .contains("SKU-XYZ")
                    .contains("25")
                    .contains("5");
        }
    }

    // ---------------------------------------------------------------
    // ErrorCode registry check for cart codes
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Cart ErrorCode constants")
    class CartErrorCodeConstants {

        @Test
        @DisplayName("CART_ITEM_NOT_FOUND has code CART-404-001")
        void cartItemNotFound_hasCorrectCode() {
            assertThat(ErrorCode.CART_ITEM_NOT_FOUND.getCode()).isEqualTo("CART-404-001");
        }

        @Test
        @DisplayName("CART_ITEM_ACCESS_DENIED has code CART-403-001")
        void cartItemAccessDenied_hasCorrectCode() {
            assertThat(ErrorCode.CART_ITEM_ACCESS_DENIED.getCode()).isEqualTo("CART-403-001");
        }

        @Test
        @DisplayName("INSUFFICIENT_STOCK has code CART-400-001")
        void insufficientStock_hasCorrectCode() {
            assertThat(ErrorCode.INSUFFICIENT_STOCK.getCode()).isEqualTo("CART-400-001");
        }

        @Test
        @DisplayName("All cart error codes have non-blank default messages")
        void allCartCodes_haveNonBlankDefaultMessages() {
            assertThat(ErrorCode.CART_ITEM_NOT_FOUND.getDefaultMessage()).isNotBlank();
            assertThat(ErrorCode.CART_ITEM_ACCESS_DENIED.getDefaultMessage()).isNotBlank();
            assertThat(ErrorCode.INSUFFICIENT_STOCK.getDefaultMessage()).isNotBlank();
        }
    }
}
