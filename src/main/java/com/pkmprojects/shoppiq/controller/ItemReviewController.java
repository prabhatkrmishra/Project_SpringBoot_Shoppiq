package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.dto.request.ItemReviewRequest;
import com.pkmprojects.shoppiq.dto.response.ItemReviewResponse;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.service.ItemReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing product reviews.
 *
 * <p>
 * Exposes endpoints for creating, retrieving, updating and deleting
 * reviews associated with catalog items.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Accept HTTP requests.</li>
 *     <li>Validate incoming request payloads.</li>
 *     <li>Delegate business logic to {@link ItemReviewService}.</li>
 *     <li>Return DTO responses.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *     <li>Contains no business logic.</li>
 *     <li>Contains no persistence logic.</li>
 *     <li>Acts solely as the HTTP boundary.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Validated
@RestController
@RequiredArgsConstructor
public class ItemReviewController {

    /**
     * Item review service.
     */
    private final ItemReviewService itemReviewService;

    /**
     * Creates a new review for an item.
     *
     * <p>
     * The reviewer identifier is temporarily supplied as a request parameter.
     * Once JWT authentication is fully integrated, the reviewer will be
     * resolved from the authenticated principal instead.
     * </p>
     *
     * @param itemId      item identifier — must be a positive number
     * @param currentUser reviewer identifier
     * @param request     review request
     * @return created review
     */
    @PostMapping("/items/{itemId}/create/review")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemReviewResponse create(
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ItemReviewRequest request
    ) {
        return itemReviewService.create(itemId, currentUser, request);
    }

    /**
     * Retrieves every review belonging to an item.
     *
     * @param itemId item identifier — must be a positive number
     * @return ordered review list
     */
    @GetMapping("/items/{itemId}/reviews")
    public List<ItemReviewResponse> getByItem(
            @PathVariable
            @Positive(message = "Item id must be a positive number")
            Long itemId
    ) {
        return itemReviewService.getByItem(itemId);
    }

    /**
     * Retrieves a review by its identifier.
     *
     * @param reviewId review identifier — must be a positive number
     * @return matching review
     */
    @GetMapping("/reviews/{reviewId}")
    public ItemReviewResponse getById(
            @PathVariable
            @Positive(message = "Review id must be a positive number")
            Long reviewId
    ) {
        return itemReviewService.getById(reviewId);
    }

    /**
     * Updates an existing review.
     *
     * @param reviewId review identifier — must be a positive number
     * @param request  updated review information
     * @return updated review
     */
    @PutMapping("/reviews/{reviewId}/update")
    public ItemReviewResponse update(
            @PathVariable
            @Positive(message = "Review id must be a positive number")
            Long reviewId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ItemReviewRequest request
    ) {
        return itemReviewService.update(reviewId, currentUser, request);
    }

    /**
     * Deletes an existing review.
     *
     * @param reviewId review identifier — must be a positive number
     */
    @DeleteMapping("/reviews/{reviewId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable
            @Positive(message = "Review id must be a positive number")
            Long reviewId,
            @AuthenticationPrincipal User currentUser
    ) {
        itemReviewService.delete(reviewId, currentUser);
    }
}
