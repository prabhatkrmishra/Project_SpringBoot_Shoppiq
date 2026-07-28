package com.pkmprojects.shoppiq.enums;

/**
 * <strong>Spring Boot Concept:</strong> Status of a contact message.
 *
 * <p>Defines the lifecycle of a customer inquiry submitted via the
 * contact form. Messages start as {@link #PENDING}, transition to
 * {@link #READ} when an admin views them, and {@link #REPLIED} when
 * the admin responds.</p>
 *
 * <h3>Spring Boot Concepts</h3>
 * <ul>
 *     <li><strong>Enum-backed status tracking</strong> — Used with
 *         {@code @Enumerated(EnumType.STRING)} in the JPA entity to
 *         store human-readable values in the database.</li>
 *     <li><strong>State machine pattern</strong> — The three values
 *         define a simple workflow (PENDING → READ → REPLIED) that is
 *         enforced at the service layer.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public enum ContactMessageStatus {
    PENDING,
    READ,
    REPLIED
}
