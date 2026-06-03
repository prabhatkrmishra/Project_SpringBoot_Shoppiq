package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.models.ItemReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemReviewRepository extends JpaRepository<ItemReview, Long> {

}
