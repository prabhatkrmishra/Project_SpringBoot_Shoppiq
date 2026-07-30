package com.pkmprojects.shoppiq.exception.general.email;

import com.pkmprojects.shoppiq.exception.business.ResourceNotFoundException;
import com.pkmprojects.shoppiq.exception.codes.ErrorCode;

import java.util.Set;

/**
 * Thrown when no email provider is registered for the requested type.
 *
 * <p>This exception is thrown when the application attempts to send an
 * email but no provider (SMTP or Console) is registered for the requested
 * type. It uses the {@link ErrorCode#EMAIL_PROVIDER_NOT_FOUND} code and
 * HTTP 404 Not Found status. The administrator must configure an email
 * provider in the application properties.</p>
 *
 * <p>The detail message includes the requested provider name and available
 * options (e.g., "No email provider found for: smtp. Available: [console,
 * smtp]") to help the administrator understand what configuration is
 * needed.</p>
 *
 * @author prabhatkrmishra
 * @see ErrorCode#EMAIL_PROVIDER_NOT_FOUND
 * @since 1.0.0
 */
public final class EmailProviderNotFoundException extends ResourceNotFoundException {

    private EmailProviderNotFoundException(String detail) {
        super(ErrorCode.EMAIL_PROVIDER_NOT_FOUND, detail);
    }

    /**
     * Creates an exception indicating that no email provider matches the
     * requested name.
     *
     * @param name      the requested provider name
     * @param available the set of available provider names
     * @return email provider not found exception
     */
    public static EmailProviderNotFoundException byName(String name, Set<String> available) {
        return new EmailProviderNotFoundException(
                "No email provider found for: %s. Available: %s".formatted(name, available)
        );
    }

    /**
     * Creates an exception indicating that no email providers are
     * configured at all.
     *
     * @return email provider not found exception
     */
    public static EmailProviderNotFoundException noneAvailable() {
        return new EmailProviderNotFoundException("No email providers available.");
    }
}
