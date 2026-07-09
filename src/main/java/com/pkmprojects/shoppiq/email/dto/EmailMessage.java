package com.pkmprojects.shoppiq.email.dto;

import com.pkmprojects.shoppiq.email.EmailType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * DTO representing an email message to be sent.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Getter
@Builder
public class EmailMessage {

    /**
     * Recipient email address.
     */
    private String to;

    /**
     * Email subject line.
     */
    private String subject;

    /**
     * Thymeleaf template name (without .html extension).
     */
    private String templateName;

    /**
     * Type of email for logging and preference checking.
     */
    private EmailType emailType;

    /**
     * Template variables to pass to Thymeleaf.
     */
    private Map<String, Object> variables;

    /**
     * User ID for logging purposes (nullable).
     */
    private Long userId;
}
