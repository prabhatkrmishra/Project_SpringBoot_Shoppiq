package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.entity.ItemReview;
import com.pkmprojects.shoppiq.service.ItemReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/item/review")
public class ItemReviewController {

    private final ItemReviewService itemReviewService;

    public ItemReviewController(ItemReviewService itemReviewService) {
        this.itemReviewService = itemReviewService;
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ItemReview> getReviewById(@PathVariable long reviewId) {
        ItemReview review = itemReviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @PostMapping("/{itemId}/create")
    public ResponseEntity<ItemReview> createItemReview(@PathVariable long itemId, @RequestBody ItemReview itemReview) {
        ItemReview review = itemReviewService.createNewItemReview(itemId, itemReview);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PutMapping("/update/{reviewId}")
    public ResponseEntity<ItemReview> updateItemReview(@PathVariable long reviewId, @RequestBody ItemReview itemReview) {
        ItemReview updatedReview = itemReviewService.updateItemReview(reviewId, itemReview);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteItemReview(@PathVariable long id) {
        itemReviewService.deleteItemReviewById(id);
        return ResponseEntity.noContent().build();
    }
}
