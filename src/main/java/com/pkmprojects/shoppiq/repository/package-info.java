/**
 * Spring Data JPA repository interfaces for all domain aggregates.
 *
 * <p>This package contains repository interfaces for every entity in the
 * domain model. Each repository extends Spring Data's {@code JpaRepository}
 * and provides derived queries, custom finder methods, and projection
 * interfaces. The repositories abstract away the persistence layer,
 * allowing service methods to interact with the database through
 * type-safe, expressive query methods.</p>
 *
 * <p>Repositories are organized by subdomain: user, role, item, category,
 * order, payment, cart, address, seller, promo, banner, contact, and
 * newsletter. Each sub-package contains the repository interface and any
 * associated projection interfaces for read-optimized queries.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
package com.pkmprojects.shoppiq.repository;
