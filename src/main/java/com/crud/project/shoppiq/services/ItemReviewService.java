package com.crud.project.shoppiq.services;

import com.crud.project.shoppiq.models.ItemReview;
import com.crud.project.shoppiq.repositories.ItemReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemReviewService {
    @Autowired
    private ItemReviewRepository itemReviewRepository;

    @Transactional
    public Optional<ItemReview> getReviewById(long id) {
        return itemReviewRepository.findById(id);
    }

    @Transactional
    public Optional<ItemReview> createNewItemReview(long itemId, ItemReview itemReview) {
        return Optional.ofNullable(itemReviewRepository.saveWithItemId(itemId, itemReview));
    }

    @Transactional
    public Optional<ItemReview> updateItemReview(long reviewId, ItemReview itemReview) {
        return itemReviewRepository.updateById(reviewId, itemReview);
    }

    @Transactional
    public void deleteItemReviewById(long id) {
        itemReviewRepository.deleteById(id);
    }
}
