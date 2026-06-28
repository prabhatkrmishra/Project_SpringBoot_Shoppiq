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
import com.pkmprojects.shoppiq.repository.UserRepository;
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
     * User repository.
     */
    private final UserRepository userRepository;

    /**
     * Creates a service instance.
     *
     * @param itemReviewRepository review repository
     * @param itemRepository       item repository
     * @param userRepository       user repository
     */
    public ItemReviewServiceImpl(
            ItemReviewRepository itemReviewRepository,
            ItemRepository itemRepository,
            UserRepository userRepository
    ) {
        this.itemReviewRepository = itemReviewRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
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
     * Retrieves an existing user.
     *
     * @param id user identifier
     * @return user
     */
    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.id(id));
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
            Long userId,
            ItemReviewRequest request
    ) {
        if (itemReviewRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw DuplicateItemReviewException.userId(userId);
        }

        Item item = findItem(itemId);
        User user = findUser(userId);

        ItemReview review = ItemReview.builder()
                .rating(request.rating())
                .review(request.review())
                .item(item)
                .user(user)
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