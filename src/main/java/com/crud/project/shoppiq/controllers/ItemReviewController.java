package com.crud.project.shoppiq.controllers;

import com.crud.project.shoppiq.models.ItemReview;
import com.crud.project.shoppiq.services.ItemReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/item/review")
public class ItemReviewController {
    @Autowired
    private ItemReviewService itemReviewService;

    @GetMapping("/{reviewId}")
    public ResponseEntity<Optional<ItemReview>> getReviewById(@PathVariable long reviewId) {
        Optional<ItemReview> review = itemReviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @PostMapping("/{itemId}/create")
    public ResponseEntity<Optional<ItemReview>> createItemReview(@PathVariable long itemId, @RequestBody ItemReview itemReview) {
        Optional<ItemReview> review = itemReviewService.createNewItemReview(itemId, itemReview);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/update/{reviewId}")
    public ResponseEntity<Optional<ItemReview>> updateItemReview(@PathVariable long reviewId, @RequestBody ItemReview itemReview) {
        Optional<ItemReview> updatedReview = itemReviewService.updateItemReview(reviewId, itemReview);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteItemReview(@PathVariable long id) {
        itemReviewService.deleteItemReviewById(id);
        return ResponseEntity.ok("deleted item review with id: " + id);
    }
}
