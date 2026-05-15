package com.crud.project.shoppiq.controllers;

import com.crud.project.shoppiq.models.ItemReview;
import com.crud.project.shoppiq.services.ItemReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/item/review")
public class ItemReviewController {
    @Autowired
    private ItemReviewService itemReviewService;

    @GetMapping("/{id}")
    public ResponseEntity<Optional<ItemReview>> getReviewById(@PathVariable long id) {
        Optional<ItemReview> item = itemReviewService.getReviewById(id);
        return ResponseEntity.ok(item);
    }
}
