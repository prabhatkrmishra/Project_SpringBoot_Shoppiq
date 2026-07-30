package com.pkmprojects.shoppiq.controller.review;

import com.pkmprojects.shoppiq.config.PaginationProperties;
import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.review.ItemReviewRequest;
import com.pkmprojects.shoppiq.dto.review.ItemReviewResponse;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.general.review.ItemReviewNotFoundException;
import com.pkmprojects.shoppiq.service.item.ItemReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for product review management.
 *
 * <p>Exposes endpoints for authenticated customers to create, read, update, and
 * delete reviews associated with catalog items. Users can only modify their own
 * reviews, and item scope is validated to prevent cross-item access. Reviews
 * submitted through this controller may require admin approval before becoming
 * visible to other users.</p>
 *
 * <p>This controller acts as the HTTP boundary for review operations. It delegates
 * all business logic — review persistence, ownership validation, item scope
 * validation, and paginated querying — to {@link ItemReviewService}. The controller
 * handles no business logic beyond page-size capping and scope validation.</p>
 *
 * <p>Mutating endpoints require authentication. Read-only endpoints are public.
 * The user's own pending reviews are visible to them even before admin approval.</p>
 *
 * <p>Supported endpoints:</p>
 *
 * <pre>
 * POST   /items/{itemId}/review/create         — create a new review
 * GET    /items/{itemId}/review/{id}           — retrieve a single review
 * PUT    /items/{itemId}/review/{id}/update    — update an existing review
 * DELETE /items/{itemId}/review/{id}/delete    — delete an existing review
 * GET    /items/{itemId}/reviews               — list reviews for an item
 * GET    /user/reviews                         — list the user's own reviews
 * </pre>
 *
 * @author prabhatkrmishra
 * @see ItemReviewService
 * @since 1.0.0
 */
@Validated
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ItemReviewService itemReviewService;
    private final PaginationProperties pagination;

    /**
     * Creates a new review for an item.
     *
     * <p>The review is associated with the authenticated user and the specified
     * item. The user must be authenticated and the item must exist. Returns
     * the created review with HTTP 201 status.</p>
     *
     * @param itemId      the item ID to review (must be positive)
     * @param currentUser the authenticated user creating the review
     * @param request     the review payload (rating, comment)
     * @return 201 Created with the created review response
     */
    @PostMapping("/items/{itemId}/review/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemReviewResponse create(
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId,
            @AuthenticationPrincipal(expression = "user") User currentUser,
            @Valid @RequestBody ItemReviewRequest request
    ) {
        return itemReviewService.create(itemId, currentUser, request);
    }

    /**
     * Retrieves a single review by its identifier, scoped to an item.
     *
     * <p>Validates that the review belongs to the specified item to prevent
     * cross-item access. Returns 404 if the review does not exist or does
     * not belong to the given item.</p>
     *
     * @param itemId the item ID (must be positive)
     * @param id     the review ID (must be positive)
     * @return 200 OK with the review response
     */
    @GetMapping("/items/{itemId}/review/{id}")
    public ItemReviewResponse getById(
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId,
            @PathVariable @Positive(message = "Review id must be a positive number") Long id
    ) {
        ItemReviewResponse response = itemReviewService.getById(id);
        if (!itemId.equals(response.itemId())) {
            throw ItemReviewNotFoundException.id(id);
        }
        return response;
    }

    /**
     * Updates an existing review.
     *
     * <p>Only the review author can update their review. The item scope is
     * validated to prevent cross-item manipulation.</p>
     *
     * @param itemId      the item ID (must be positive)
     * @param id          the review ID (must be positive)
     * @param currentUser the authenticated user (must be the review author)
     * @param request     the updated review payload (validated via @Valid)
     * @return 200 OK with the updated review response
     */
    @PutMapping("/items/{itemId}/review/{id}/update")
    public ItemReviewResponse update(
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId,
            @PathVariable @Positive(message = "Review id must be a positive number") Long id,
            @AuthenticationPrincipal(expression = "user") User currentUser,
            @Valid @RequestBody ItemReviewRequest request
    ) {
        validateItemScope(itemId, id);
        return itemReviewService.update(id, currentUser, request);
    }

    /**
     * Deletes an existing review.
     *
     * <p>Only the review author can delete their review. The item scope is
     * validated to prevent cross-item deletion.</p>
     *
     * @param itemId      the item ID (must be positive)
     * @param id          the review ID (must be positive)
     * @param currentUser the authenticated user (must be the review author)
     */
    @DeleteMapping("/items/{itemId}/review/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId,
            @PathVariable @Positive(message = "Review id must be a positive number") Long id,
            @AuthenticationPrincipal(expression = "user") User currentUser
    ) {
        validateItemScope(itemId, id);
        itemReviewService.delete(id, currentUser);
    }

    /**
     * Retrieves every review belonging to an item visible to the current user.
     *
     * <p>Returns approved reviews for all users. The authenticated user also
     * sees their own pending reviews. Results are paginated.</p>
     *
     * @param itemId the item ID (must be positive)
     * @param page   zero-based page index
     * @param size   page size (capped by the configured maximum)
     * @return 200 OK with page of review responses
     */
    @GetMapping("/items/{itemId}/reviews")
    public PageResponse<ItemReviewResponse> getByItem(
            @PathVariable @Positive(message = "Item id must be a positive number") Long itemId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        size = Math.min(size, pagination.maxPageSize());
        return itemReviewService.getByItemForUser(itemId, null, page, size);
    }

    /**
     * Retrieves every review written by the authenticated user.
     *
     * <p>Returns all reviews (including pending/rejected) authored by the user.
     * Results are paginated.</p>
     *
     * @param currentUser the authenticated user
     * @param page        zero-based page index
     * @param size        page size (capped by the configured maximum)
     * @return 200 OK with page of review responses
     */
    @GetMapping("/user/reviews")
    public PageResponse<ItemReviewResponse> getByUser(
            @AuthenticationPrincipal(expression = "user") User currentUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        size = Math.min(size, pagination.maxPageSize());
        return itemReviewService.getByUser(currentUser, page, size);
    }

    /**
     * Validates that the review identified by {@code reviewId} belongs to the
     * item identified by {@code itemId}. Throws {@code ItemReviewNotFoundException}
     * if the relationship doesn't hold, preventing cross-item access.
     *
     * @param itemId   the item ID
     * @param reviewId the review ID
     */
    private void validateItemScope(Long itemId, Long reviewId) {
        ItemReviewResponse response = itemReviewService.getById(reviewId);
        if (!itemId.equals(response.itemId())) {
            throw ItemReviewNotFoundException.id(reviewId);
        }
    }
}
