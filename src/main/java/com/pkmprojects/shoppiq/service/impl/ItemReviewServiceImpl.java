package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.common.PageResponse;
import com.pkmprojects.shoppiq.dto.request.ItemReviewRequest;
import com.pkmprojects.shoppiq.dto.response.ItemReviewResponse;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemReview;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.DuplicateItemReviewException;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.ItemReviewAccessDeniedException;
import com.pkmprojects.shoppiq.exception.ItemReviewNotFoundException;
import com.pkmprojects.shoppiq.exception.UserNotFoundException;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.repository.ItemReviewRepository;
import com.pkmprojects.shoppiq.service.ItemReviewService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link ItemReviewService}.
 *
 * <p>
 * Responsible for managing product reviews including creation,
 * retrieval, update and deletion.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Create reviews.</li>
 *     <li>Retrieve reviews.</li>
 *     <li>Update reviews.</li>
 *     <li>Delete reviews.</li>
 *     <li>Validate referenced users and items.</li>
 * </ul>
 *
 * @author PrabhatKrMishra
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
     * Item repository.
     */
    private final ItemRepository itemRepository;

    /**
     * Creates a service instance.
     *
     * @param itemReviewRepository review repository
     * @param itemRepository       item repository
     */
    public ItemReviewServiceImpl(
            ItemReviewRepository itemReviewRepository,
            ItemRepository itemRepository
    ) {
        this.itemReviewRepository = itemReviewRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Retrieves an existing item.
     *
     * @param id item identifier
     * @return item
     */
    private Item findItem(Long id) {

        return itemRepository.findById(id)
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
        List<ItemReviewResponse> all = getByItemForUser(itemId, currentUser);
        int start = Math.min(page * size, all.size());
        int end = Math.min(start + size, all.size());
        List<ItemReviewResponse> content = all.subList(start, end);
        int totalPages = (all.isEmpty()) ? 0 : (int) Math.ceil((double) all.size() / size);
        return new PageResponse<>(content, page, size, all.size(), totalPages, page == 0, page >= totalPages - 1);
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