package com.pkmprojects.shoppiq.repository.banner;

import com.pkmprojects.shoppiq.entity.banner.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Spring Data JPA repository for {@link Banner} persistence operations.
 *
 * <p><strong>What Spring Data JPA demonstrates here:</strong></p>
 * <ul>
 *   <li><strong>Derived query methods with ordering</strong> — {@code OrderByDisplayOrderAsc}
 *       appends {@code ORDER BY display_order ASC} to the generated SQL.</li>
 *   <li><strong>Property path expressions</strong> — {@code findAllByActiveTrue} resolves
 *       {@code active} as a boolean field and generates
 *       {@code SELECT * FROM banners WHERE active = TRUE}.</li>
 *   <li><strong>{@code @Modifying} + {@code @Query}</strong> — {@code toggleActive} shows
 *       an atomic {@code UPDATE} with expression in JPQL that cannot be expressed via
 *       method naming alone.</li>
 * </ul>
 *
 * <p><strong>Method naming → SQL translation examples:</strong></p>
 * <pre>
 *   findAllByActiveTrueOrderByDisplayOrderAsc
 *       → SELECT * FROM banners WHERE active = TRUE ORDER BY display_order ASC
 *   toggleActive(@Query)
 *       → UPDATE banners SET active = NOT active WHERE id = ?
 * </pre>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    /**
     * Returns all active banners sorted by display order.
     *
     * <p>Used by the homepage to render the Sales &amp; Offers section.</p>
     *
     * @return ordered list of active banners
     */
    List<Banner> findAllByActiveTrueOrderByDisplayOrderAsc();

    /**
     * Atomically flips the active status of a banner.
     *
     * @param id banner ID
     * @return number of rows affected
     */
    @Modifying
    @Query("UPDATE Banner b SET b.active = NOT b.active WHERE b.id = :id")
    int toggleActive(@Param("id") Long id);
}
