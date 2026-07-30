package com.pkmprojects.shoppiq.exception.general.promo;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Thrown when a requested promo code could not be found.
 *
 * <p>This exception is thrown when a customer applies a promo code that
 * does not exist in the system. It uses the
 * {@link ErrorCode#PROMO_CODE_NOT_FOUND} code and HTTP 404 Not Found
 * status. The code may be misspelled or expired and removed from the
 * database.</p>
 *
 * <p>The detail message includes the promo code string (e.g.,
 * "Promo code 'SUMMER2026' was not found.") to help the client
 * understand which code was invalid. The client should verify the code
 * and retry.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#PROMO_CODE_NOT_FOUND
 * @since 1.0.0
 */
public final class PromoCodeNotFoundException extends ResourceNotFoundException {

    private PromoCodeNotFoundException(String detail) {
        super(ErrorCode.PROMO_CODE_NOT_FOUND, detail);
    }

    /**
     * Creates an exception for a promo code not found by its code string.
     *
     * @param code the promo code string
     * @return a new exception instance
     */
    public static PromoCodeNotFoundException forCode(String code) {
        return new PromoCodeNotFoundException(
                "Promo code '%s' was not found.".formatted(code)
        );
    }

    /**
     * Creates an exception for a promo code not found by its ID.
     *
     * @param id the promo code ID
     * @return a new exception instance
     */
    public static PromoCodeNotFoundException forId(Long id) {
        return new PromoCodeNotFoundException(
                "Promo code with id '%d' was not found.".formatted(id)
        );
    }
}
