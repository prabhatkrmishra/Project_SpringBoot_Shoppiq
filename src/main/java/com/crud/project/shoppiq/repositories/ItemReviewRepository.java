package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.models.ItemReview;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemReviewRepository extends CrudRepository<ItemReview, Long> {

}
