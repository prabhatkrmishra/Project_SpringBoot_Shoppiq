package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemReview;
import com.pkmprojects.shoppiq.exception.ItemReviewNotFoundException;
import com.pkmprojects.shoppiq.repository.ItemReviewRepository;
import org.springframework.stereotype.Service;

/**
 * Service for Item review CRUD operations.
 *
 * <p>
 * Methods that look up a single review throw {@link ItemReviewNotFoundException}
 * when the resource does not exist, rather than returning an empty
 * {@code Optional} for the caller to re-check.
 * </p>
 */
@Service
public class ItemReviewService {

    private final ItemReviewRepository itemReviewRepository;
    private final ItemService itemService;

    public ItemReviewService(ItemReviewRepository itemReviewRepository, ItemService itemService) {
        this.itemReviewRepository = itemReviewRepository;
        this.itemService = itemService;
    }

    public ItemReview getReviewById(long reviewId) {
        return itemReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ItemReviewNotFoundException("item review with id: " + reviewId + " not found"));
    }

    public ItemReview createNewItemReview(long itemId, ItemReview itemReview) {
        Item currentItem = itemService.getItemById(itemId);
        itemReview.setItem(currentItem);
        return itemReviewRepository.save(itemReview);
    }

    public ItemReview updateItemReview(long reviewId, ItemReview itemReview) {
        ItemReview currentItemReview = getReviewById(reviewId);
        currentItemReview.update(itemReview);
        return itemReviewRepository.save(currentItemReview);
    }

    public void deleteItemReviewById(long reviewId) {
        if (!itemReviewRepository.existsById(reviewId)) {
            throw new ItemReviewNotFoundException("item review with id: " + reviewId + " not found");
        }
        itemReviewRepository.deleteById(reviewId);
    }
}
