package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.user.User;

import java.time.Instant;
import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Read-only user query facade for admin dashboards and reports.
 *
 * <h2>Role in Layered Architecture</h2>
 * <p>
 * A ReadModel facade that decouples admin services from {@code UserRepository}.
 * Provides aggregate queries over user data for dashboards and reports.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Count total users.</li>
 *   <li>Count users created after a given date (new registrations).</li>
 *   <li>Retrieve the 10 most recently created users for activity feeds.</li>
 * </ul>
 *
 * <p>Decouples admin services from {@code UserRepository},
 * providing aggregate queries over user data.</p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
public interface AdminUserReadModel {

    /**
     * Returns total user count.
     */
    long countAll();

    /**
     * Returns users created after the given instant.
     */
    long countCreatedAfter(Instant instant);

    /**
     * Returns the 10 most recently created users.
     */
    List<User> findRecentTop10();
}
