package com.pkmprojects.shoppiq.exception.validation;

import com.pkmprojects.shoppiq.exception.business.InvalidOperationException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

/**
 * Exception thrown when a business validation rule is violated.
 *
 * <p>
 * This exception represents domain validation failures and should not be
 * confused with Bean Validation exceptions triggered by Jakarta Validation.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Represent business validation failures.</li>
 *     <li>Provide a standardized exception for invalid business operations.</li>
 *     <li>Associate the failure with {@link ErrorCode#VALIDATION_FAILED}.</li>
 * </ul>
 *
 * <h2>Typical Usage</h2>
 * <pre>{@code
 * if (cart.isEmpty()) {
 *     throw new ValidationException("Cannot checkout an empty cart.");
 * }
 * }</pre>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
public class ValidationException extends InvalidOperationException {

    /**
     * Creates a new business validation exception.
     *
     * @param detail detailed validation failure message
     */
    public ValidationException(String detail) {
        super(ErrorCode.VALIDATION_FAILED, detail);
    }
}