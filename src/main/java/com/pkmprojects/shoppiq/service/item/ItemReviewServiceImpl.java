package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.review.ItemReviewRequest;
import com.pkmprojects.shoppiq.dto.review.ItemReviewResponse;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.enums.ReviewStatus;
import com.pkmprojects.shoppiq.exception.general.item.DuplicateItemReviewException;
import com.pkmprojects.shoppiq.exception.general.item.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.general.review.ItemReviewAccessDeniedException;
import com.pkmprojects.shoppiq.exception.general.review.ItemReviewNotFoundException;
import com.pkmprojects.shoppiq.exception.general.user.UserNotFoundException;
import com.pkmprojects.shoppiq.repository.item.ItemReviewRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link ItemReviewService}
 * containing business logic for product review management.
 *
 * <p>Handles review creation (with seller/admin prevention and duplicate enforcement),
 * retrieval with visibility filtering for APPROVED vs user-own reviews, and update/deletion
 * with ownership or admin authorization. Used by {@code ItemReviewController}.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Review write operations are atomic; reads use {@code readOnly = true}.</li>
 *   <li><strong>Constructor injection</strong> — final fields for immutability and testability.</li>
 * </ul>
 * </p>
 *
 * @author prabhatkrmishra
 * @see ItemReviewService
 * @since 1.0.0
 */
@Service
@Transactional
public class ItemReviewServiceImpl implements ItemReviewService {

    /**
     * Review repository.
     */
    private final ItemReviewRepository itemReviewRepository;

    /**
     * Item lookup service.
     */
    private final ItemLookupService itemLookupService;

    /**
     * Creates a service instance.
     *
     * @param itemReviewRepository review repository
     * @param itemLookupService    item lookup service
     */
    public ItemReviewServiceImpl(
            ItemReviewRepository itemReviewRepository,
            ItemLookupService itemLookupService
    ) {
        this.itemReviewRepository = itemReviewRepository;
        this.itemLookupService = itemLookupService;
    }

    /**
     * Retrieves an existing item.
     *
     * @param id item identifier
     * @return item
     */
    private Item findItem(Long id) {

        return itemLookupService.findById(id)
                .orElseThrow(() ->
                        ItemNotFoundException.id(id));
    }

    /**
     * Retrieves an existing review.
     *
     * @param id review identifier
     * @return review
     */
    private ItemReview findReview(Long id) {

        return itemReviewRepository.findById(id)
                .orElseThrow(() ->
                        ItemReviewNotFoundException.id(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemReviewResponse create(
            Long itemId,
            User currentUser,
            ItemReviewRequest request
    ) {
        if (currentUser == null) {
            throw UserNotFoundException.unknown("Creating new item response");
        }

        boolean isSeller = (currentUser.getRoles() != null) &&
                currentUser.getRoles().stream()
                        .anyMatch(role -> "ROLE_SELLER".equals(role.getRoleName()));
        if (isSeller) {
            throw ItemReviewAccessDeniedException.sellerCannotReview();
        }

        boolean isAdmin = (currentUser.getRoles() != null) &&
                currentUser.getRoles().stream()
                        .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName()));
        if (isAdmin) {
            throw ItemReviewAccessDeniedException.adminCannotReview();
        }

        if (itemReviewRepository.existsByUserIdAndItemId(currentUser.getId(), itemId)) {
            throw DuplicateItemReviewException.userId(currentUser.getId());
        }

        Item item = findItem(itemId);

        ItemReview review = ItemReview.builder()
                .rating(request.rating())
                .review(request.review())
                .item(item)
                .user(currentUser)
                .build();

        return ItemReviewResponse.fromEntity(
                itemReviewRepository.save(review)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemReviewResponse getById(Long reviewId) {
        return ItemReviewResponse.fromEntity(
                findReview(reviewId)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ItemReviewResponse> getByItemForUser(Long itemId, User currentUser) {
        findItem(itemId);

        Long userId = (currentUser != null) ? currentUser.getId() : null;

        if (userId != null) {
            return itemReviewRepository
                    .findVisibleReviewsForUser(itemId, userId)
                    .stream()
                    .map(ItemReviewResponse::fromEntity)
                    .toList();
        }

        return itemReviewRepository
                .findAllByItemIdAndStatusOrderByCreatedAtDesc(
                        itemId, com.pkmprojects.shoppiq.enums.ReviewStatus.APPROVED)
                .stream()
                .map(ItemReviewResponse::fromEntity)
                .toList();
    }

    @Override
    public PageResponse<ItemReviewResponse> getByItemForUser(Long itemId, User currentUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long userId = (currentUser != null) ? currentUser.getId() : null;

        Page<ItemReview> reviewPage;
        if (userId != null) {
            reviewPage = itemReviewRepository.findVisibleReviewsForUser(itemId, userId, pageable);
        } else {
            reviewPage = itemReviewRepository.findAllByItemIdAndStatusOrderByCreatedAtDesc(
                    itemId, ReviewStatus.APPROVED, pageable);
        }

        return PageResponse.of(reviewPage, ItemReviewResponse::fromEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ItemReviewResponse> getByUser(User user) {
        return itemReviewRepository
                .findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ItemReviewResponse::fromEntity)
                .toList();
    }

    @Override
    public PageResponse<ItemReviewResponse> getByUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var reviewPage = itemReviewRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return PageResponse.of(reviewPage, ItemReviewResponse::fromEntity);
    }

    /**
     * Verifies that the supplied user is allowed to modify the given
     * review — either because they wrote it, or because they hold the
     * {@code ROLE_ADMIN} authority.
     *
     * @param review      review being modified
     * @param currentUser caller attempting the modification
     */
    private void checkOwnership(ItemReview review, User currentUser) {

        if (currentUser == null) {
            throw ItemReviewAccessDeniedException.forReview(review.getId());
        }

        boolean isOwner = (review.getUser() != null) &&
                review.getUser().getId().equals(currentUser.getId());

        boolean isAdmin = (currentUser.getRoles() != null) &&
                currentUser.getRoles().stream()
                        .anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName()));

        if (!isOwner && !isAdmin) {
            throw ItemReviewAccessDeniedException.forReview(review.getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemReviewResponse update(
            Long reviewId,
            User currentUser,
            ItemReviewRequest request
    ) {
        ItemReview review = findReview(reviewId);
        checkOwnership(review, currentUser);

        review.setRating(request.rating());
        review.setReview(request.review());

        return ItemReviewResponse.fromEntity(
                itemReviewRepository.save(review)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Long reviewId, User currentUser) {
        ItemReview review = findReview(reviewId);
        checkOwnership(review, currentUser);

        itemReviewRepository.delete(review);
    }
}
