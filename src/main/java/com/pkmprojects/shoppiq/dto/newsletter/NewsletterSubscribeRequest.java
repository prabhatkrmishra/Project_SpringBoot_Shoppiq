package com.pkmprojects.shoppiq.dto.newsletter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for subscribing to the platform newsletter.
 *
 * <p>This record is a single-field DTO submitted to
 * {@code POST /api/newsletter/subscribe} when a visitor or customer
 * wants to receive promotional emails, product announcements, and
 * platform updates. The email address is validated for format
 * correctness and checked for uniqueness at the service layer to
 * prevent duplicate subscriptions.</p>
 *
 * <p>The single-field design demonstrates how minimal a request DTO
 * needs to be when the API action requires only one piece of data.
 * The {@code @NotBlank} and {@code @Email} annotations ensure
 * the submitted value is non-empty and matches a valid email format
 * before reaching the service layer.</p>
 *
 * @param email email address to subscribe to the newsletter; must be
 *              a valid email format; checked for uniqueness at the
 *              service layer; existing subscribers receive a
 *              no-op success response to prevent email enumeration
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record NewsletterSubscribeRequest(

        /**
         * Email address for newsletter subscription. Must be valid.
         */
        @NotBlank(message = "Email is required.")
        @Email(message = "Please provide a valid email address.")
        String email
) {
}
