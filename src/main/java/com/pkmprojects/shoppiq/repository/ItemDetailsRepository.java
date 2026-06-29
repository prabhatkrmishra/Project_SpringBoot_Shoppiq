package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.ItemDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link ItemDetails} persistence operations.
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Repository
public interface ItemDetailsRepository extends JpaRepository<ItemDetails, Long> {
}
