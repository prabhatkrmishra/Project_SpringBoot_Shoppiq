package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Banner} persistence operations.
 *
 * @author PrabhatKrMishra
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
