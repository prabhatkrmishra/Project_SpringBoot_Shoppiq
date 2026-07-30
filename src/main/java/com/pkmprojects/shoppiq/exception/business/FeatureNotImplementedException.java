package com.pkmprojects.shoppiq.exception.business;

import com.pkmprojects.shoppiq.exception.base.ShoppiqException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested feature or operation is not yet implemented.
 *
 * <p>This exception is used as a placeholder for functionality that is
 * planned but not yet available. It uses the
 * {@link ErrorCode#FEATURE_NOT_IMPLEMENTED} code and HTTP 501 Not
 * Implemented status. The detail message includes the name of the
 * unimplemented feature to help clients understand what is missing.</p>
 *
 * <p>This exception should be replaced with a proper implementation as
 * soon as the feature is developed. It is not intended to be a permanent
 * part of the error hierarchy. Use the static factory method
 * {@link #of(String)} to create an instance with a feature name.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#FEATURE_NOT_IMPLEMENTED
 * @since 1.0.0
 */
public final class FeatureNotImplementedException extends ShoppiqException {

    private FeatureNotImplementedException(String detail) {
        super(ErrorCode.FEATURE_NOT_IMPLEMENTED, HttpStatus.NOT_IMPLEMENTED, detail);
    }

    /**
     * Creates an exception indicating that the given feature is not yet
     * implemented.
     *
     * @param feature the feature or operation name
     * @return feature not implemented exception
     */
    public static FeatureNotImplementedException of(String feature) {
        return new FeatureNotImplementedException(
                "%s is not yet implemented.".formatted(feature)
        );
    }
}
