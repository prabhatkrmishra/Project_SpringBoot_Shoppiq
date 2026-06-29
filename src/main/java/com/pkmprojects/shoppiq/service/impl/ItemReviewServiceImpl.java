package com.pkmprojects.shoppiq.service.impl;

import com.pkmprojects.shoppiq.dto.request.ItemReviewRequest;
import com.pkmprojects.shoppiq.dto.response.ItemReviewResponse;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.ItemReview;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.DuplicateItemReviewException;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.exception.ItemReviewNotFoundException;
import com.pkmprojects.shoppiq.exception.UserNotFoundException;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.repository.ItemReviewRepository;
import com.pkmprojects.shoppiq.service.ItemReviewService;
import jakarta.transaction.Transactional;
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
    public List<ItemReviewResponse> getByItem(Long itemId) {
        findItem(itemId);

        return itemReviewRepository
                .findAllByItemIdOrderByCreatedAtDesc(itemId)
                .stream()
                .map(ItemReviewResponse::fromEntity)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemReviewResponse update(
            Long reviewId,
            ItemReviewRequest request
    ) {
        ItemReview review = findReview(reviewId);
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
    public void delete(Long reviewId) {
        ItemReview review = findReview(reviewId);
        itemReviewRepository.delete(review);
    }
}