package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.ItemReviewNotFoundException;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemReview;
import com.pkmprojects.shoppiq.repository.ItemReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemReviewService {
    @Autowired
    private ItemReviewRepository itemReviewRepository;

    @Autowired
    private ItemService itemService;

    public Optional<ItemReview> getReviewById(long reviewId) {
        Optional<ItemReview> currentItemReview = itemReviewRepository.findById(reviewId);
        if (currentItemReview.isPresent()) {
            return currentItemReview;
        }

        throw new ItemReviewNotFoundException("item review with id: " + reviewId + " not found");
    }

    public Optional<ItemReview> createNewItemReview(long itemId, ItemReview itemReview) {
        Optional<Item> currentItem = itemService.getItemById(itemId);
        if (currentItem.isPresent()) {
            itemReview.setItem(currentItem.get());
            itemReviewRepository.save(itemReview);
            return Optional.of(itemReview);
        }

        throw new ItemNotFoundException("Item with id: " + itemId + " not found");
    }

    public Optional<ItemReview> updateItemReview(long reviewId, ItemReview itemReview) {
        Optional<ItemReview> currentItemReview = itemReviewRepository.findById(reviewId);
        if (currentItemReview.isPresent()) {
            currentItemReview.get().update(itemReview);
            itemReviewRepository.save(currentItemReview.get());
            return Optional.of(itemReview);
        }

        throw new ItemReviewNotFoundException("item review with id: " + reviewId + " not found");
    }

    public void deleteItemReviewById(long reviewId) {
        Optional<ItemReview> currentItemReview = itemReviewRepository.findById(reviewId);
        if (currentItemReview.isPresent()) {
            itemReviewRepository.deleteById(reviewId);
            return;
        }

        throw new ItemReviewNotFoundException("item review with id: " + reviewId + " not found");
    }
}
