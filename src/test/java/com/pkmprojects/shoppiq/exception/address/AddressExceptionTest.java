package com.pkmprojects.shoppiq.exception.address;

import com.pkmprojects.shoppiq.exception.AddressAccessDeniedException;
import com.pkmprojects.shoppiq.exception.AddressNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for address-domain exception classes.
 *
 * <p>
 * Verifies that each exception carries the correct {@link ErrorCode},
 * HTTP status, and produces a meaningful detail message.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@DisplayName("Address Exception Tests")
class AddressExceptionTest {

    // ---------------------------------------------------------------
    // AddressNotFoundException
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("AddressNotFoundException")
    class AddressNotFoundExceptionTests {

        @Test
        @DisplayName("id() factory sets ADDRESS_NOT_FOUND error code")
        void id_setsCorrectErrorCode() {
            AddressNotFoundException ex = AddressNotFoundException.id(42L);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
        }

        @Test
        @DisplayName("id() factory sets HTTP 404 status")
        void id_setsHttp404() {
            AddressNotFoundException ex = AddressNotFoundException.id(42L);
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("id() detail message contains the supplied address ID")
        void id_detailContainsId() {
            AddressNotFoundException ex = AddressNotFoundException.id(99L);
            assertThat(ex.getDetail()).contains("99");
        }

        @Test
        @DisplayName("id() is a RuntimeException")
        void id_isRuntimeException() {
            assertThat(AddressNotFoundException.id(1L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ---------------------------------------------------------------
    // AddressAccessDeniedException
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("AddressAccessDeniedException")
    class AddressAccessDeniedExceptionTests {

        @Test
        @DisplayName("forAddress() factory sets ADDRESS_ACCESS_DENIED error code")
        void forAddress_setsCorrectErrorCode() {
            AddressAccessDeniedException ex = AddressAccessDeniedException.forAddress(5L);
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ADDRESS_ACCESS_DENIED);
        }

        @Test
        @DisplayName("forAddress() factory sets HTTP 403 status")
        void forAddress_setsHttp403() {
            AddressAccessDeniedException ex = AddressAccessDeniedException.forAddress(5L);
            assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("forAddress() detail message contains the supplied address ID")
        void forAddress_detailContainsAddressId() {
            AddressAccessDeniedException ex = AddressAccessDeniedException.forAddress(77L);
            assertThat(ex.getDetail()).contains("77");
        }

        @Test
        @DisplayName("forAddress() is a RuntimeException")
        void forAddress_isRuntimeException() {
            assertThat(AddressAccessDeniedException.forAddress(1L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ---------------------------------------------------------------
    // ErrorCode registry check for address codes
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Address ErrorCode constants")
    class AddressErrorCodeConstants {

        @Test
        @DisplayName("ADDRESS_NOT_FOUND has code ADDRESS-404-001")
        void addressNotFound_hasCorrectCode() {
            assertThat(ErrorCode.ADDRESS_NOT_FOUND.getCode()).isEqualTo("ADDRESS-404-001");
        }

        @Test
        @DisplayName("ADDRESS_ACCESS_DENIED has code ADDRESS-403-001")
        void addressAccessDenied_hasCorrectCode() {
            assertThat(ErrorCode.ADDRESS_ACCESS_DENIED.getCode()).isEqualTo("ADDRESS-403-001");
        }

        @Test
        @DisplayName("All address error codes have non-blank default messages")
        void allAddressCodes_haveNonBlankDefaultMessages() {
            assertThat(ErrorCode.ADDRESS_NOT_FOUND.getDefaultMessage()).isNotBlank();
            assertThat(ErrorCode.ADDRESS_ACCESS_DENIED.getDefaultMessage()).isNotBlank();
        }

        @Test
        @DisplayName("Address error codes are distinct from all other error codes")
        void addressCodes_areUniqueInRegistry() {
            long count404 = java.util.Arrays.stream(ErrorCode.values())
                    .filter(ec -> ec.getCode().equals("ADDRESS-404-001"))
                    .count();
            long count403 = java.util.Arrays.stream(ErrorCode.values())
                    .filter(ec -> ec.getCode().equals("ADDRESS-403-001"))
                    .count();

            assertThat(count404).isEqualTo(1);
            assertThat(count403).isEqualTo(1);
        }
    }
}