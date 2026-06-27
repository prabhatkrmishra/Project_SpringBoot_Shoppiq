package com.pkmprojects.shoppiq.repository;

import com.pkmprojects.shoppiq.entity.ItemReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemReviewRepository extends JpaRepository<ItemReview, Long> {

}
