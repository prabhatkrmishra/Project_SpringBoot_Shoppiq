package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Category} persistence operations.
 *
 * <p>
 * Provides CRUD operations together with additional query methods required
 * by the catalog module.
 * </p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its URL-friendly slug.
     *
     * @param slug category slug
     * @return matching category, if present
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Finds a category by its name ignoring character case.
     *
     * @param name category name
     * @return matching category, if present
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Determines whether another category already exists with the supplied
     * name while excluding the specified identifier.
     *
     * <p>
     * Primarily used during update operations to ignore the entity currently
     * being modified.
     * </p>
     *
     * @param name category name
     * @param id   category identifier to exclude
     * @return {@code true} if another matching category exists
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Determines whether a category already exists with the supplied slug.
     *
     * @param slug category slug
     * @return {@code true} if found
     */
    boolean existsBySlug(String slug);

    List<Category> findAllByOrderByNameAsc();
}