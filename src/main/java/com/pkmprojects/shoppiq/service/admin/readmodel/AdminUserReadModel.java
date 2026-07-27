package com.pkmprojects.shoppiq.service.admin.readmodel;

import com.pkmprojects.shoppiq.entity.user.User;

import java.time.Instant;
import java.util.List;

/**
 * Read-only user query facade for admin dashboards and reports.
 *
 * <p>Decouples admin services from {@code UserRepository},
 * providing aggregate queries over user data.</p>
 *
 * @author PrabhatKrMishra
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
