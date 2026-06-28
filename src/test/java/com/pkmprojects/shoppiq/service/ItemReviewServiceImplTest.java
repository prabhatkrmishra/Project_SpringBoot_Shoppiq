package com.pkmprojects.shoppiq.service;

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
import com.pkmprojects.shoppiq.service.impl.ItemReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ItemReviewServiceImpl}.
 *
 * <p>All dependencies are mocked; no database or Spring context is involved.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ItemReviewServiceImpl Tests")
class ItemReviewServiceImplTest {

    @Mock
    private ItemReviewRepository itemReviewRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemReviewServiceImpl reviewService;

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Sets the private {@code id} declared in {@code BaseEntity} on any
     * {@code AuditableEntity} subclass without requiring a setter.
     */
    private static void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    private Item stubItem;
    private User stubUser;
    private ItemReview stubReview;

    @BeforeEach
    void setUp() throws Exception {
        stubItem = Item.builder()
                .name("Laptop")
                .description("A powerful laptop")
                .build();
        setId(stubItem, 10L);

        stubUser = User.builder()
                .name("Alice")
                .username("alice")
                .email("alice@example.com")
                .password("hashed")
                .enabled(true)
                .build();
        setId(stubUser, 1L);

        stubReview = ItemReview.builder()
                .rating(4)
                .review("Great product!")
                .item(stubItem)
                .user(stubUser)
                .build();
        setId(stubReview, 100L);
    }

    // ---------------------------------------------------------------
    // create()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Creates and returns a review when no prior review exists")
        void create_happyPath_returnsResponse() {
            ItemReviewRequest request = new ItemReviewRequest(5, "Excellent!");

            when(itemReviewRepository.existsByUserIdAndItemId(1L, 10L)).thenReturn(false);
            when(itemRepository.findById(10L)).thenReturn(Optional.of(stubItem));
            when(userRepository.findById(1L)).thenReturn(Optional.of(stubUser));
            when(itemReviewRepository.save(any(ItemReview.class))).thenReturn(stubReview);

            ItemReviewResponse response = reviewService.create(10L, 1L, request);

            assertThat(response).isNotNull();
            assertThat(response.rating()).isEqualTo(4);            // from stubReview
            assertThat(response.reviewerUsername()).isEqualTo("alice");

            ArgumentCaptor<ItemReview> captor = ArgumentCaptor.forClass(ItemReview.class);
            verify(itemReviewRepository).save(captor.capture());
            assertThat(captor.getValue().getRating()).isEqualTo(5); // from request
            assertThat(captor.getValue().getReview()).isEqualTo("Excellent!");
        }

        @Test
        @DisplayName("Throws DuplicateItemReviewException when user already reviewed the item")
        void create_duplicateReview_throwsDuplicateException() {
            when(itemReviewRepository.existsByUserIdAndItemId(1L, 10L)).thenReturn(true);

            ItemReviewRequest request = new ItemReviewRequest(3, "Second attempt");

            assertThatThrownBy(() -> reviewService.create(10L, 1L, request))
                    .isInstanceOf(DuplicateItemReviewException.class);

            verify(itemRepository, never()).findById(any());
            verify(itemReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws ItemNotFoundException when item does not exist")
        void create_itemNotFound_throwsItemNotFoundException() {
            when(itemReviewRepository.existsByUserIdAndItemId(1L, 99L)).thenReturn(false);
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            ItemReviewRequest request = new ItemReviewRequest(3, "Good");

            assertThatThrownBy(() -> reviewService.create(99L, 1L, request))
                    .isInstanceOf(ItemNotFoundException.class);
        }

        @Test
        @DisplayName("Throws UserNotFoundException when user does not exist")
        void create_userNotFound_throwsUserNotFoundException() {
            when(itemReviewRepository.existsByUserIdAndItemId(99L, 10L)).thenReturn(false);
            when(itemRepository.findById(10L)).thenReturn(Optional.of(stubItem));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            ItemReviewRequest request = new ItemReviewRequest(3, "Good");

            assertThatThrownBy(() -> reviewService.create(10L, 99L, request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // ---------------------------------------------------------------
    // getById()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("Returns a review when it exists")
        void getById_found_returnsResponse() {
            when(itemReviewRepository.findById(100L)).thenReturn(Optional.of(stubReview));

            ItemReviewResponse response = reviewService.getById(100L);

            assertThat(response.id()).isEqualTo(100L);
            assertThat(response.reviewerName()).isEqualTo("Alice");
            assertThat(response.rating()).isEqualTo(4);
        }

        @Test
        @DisplayName("Throws ItemReviewNotFoundException when review does not exist")
        void getById_notFound_throwsException() {
            when(itemReviewRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.getById(999L))
                    .isInstanceOf(ItemReviewNotFoundException.class);
        }
    }

    // ---------------------------------------------------------------
    // getByItem()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getByItem()")
    class GetByItem {

        @Test
        @DisplayName("Returns all reviews for an existing item")
        void getByItem_existingItem_returnsList() {
            when(itemRepository.findById(10L)).thenReturn(Optional.of(stubItem));
            when(itemReviewRepository.findAllByItemIdOrderByCreatedAtDesc(10L))
                    .thenReturn(List.of(stubReview));

            List<ItemReviewResponse> result = reviewService.getByItem(10L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).rating()).isEqualTo(4);
        }

        @Test
        @DisplayName("Throws ItemNotFoundException when item does not exist")
        void getByItem_itemNotFound_throwsException() {
            when(itemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.getByItem(999L))
                    .isInstanceOf(ItemNotFoundException.class);

            verify(itemReviewRepository, never()).findAllByItemIdOrderByCreatedAtDesc(any());
        }

        @Test
        @DisplayName("Returns an empty list when an item has no reviews")
        void getByItem_noReviews_returnsEmptyList() {
            when(itemRepository.findById(10L)).thenReturn(Optional.of(stubItem));
            when(itemReviewRepository.findAllByItemIdOrderByCreatedAtDesc(10L))
                    .thenReturn(List.of());

            List<ItemReviewResponse> result = reviewService.getByItem(10L);

            assertThat(result).isEmpty();
        }
    }

    // ---------------------------------------------------------------
    // update()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Updates rating and review text and returns updated response")
        void update_validRequest_returnsUpdatedResponse() throws Exception {
            ItemReview updatedReview = ItemReview.builder()
                    .rating(2)
                    .review("Changed my mind.")
                    .item(stubItem)
                    .user(stubUser)
                    .build();
            setId(updatedReview, 100L);

            when(itemReviewRepository.findById(100L)).thenReturn(Optional.of(stubReview));
            when(itemReviewRepository.save(stubReview)).thenReturn(updatedReview);

            ItemReviewRequest request = new ItemReviewRequest(2, "Changed my mind.");
            ItemReviewResponse response = reviewService.update(100L, request);

            assertThat(response.rating()).isEqualTo(2);
            assertThat(response.review()).isEqualTo("Changed my mind.");

            // Verify mutation applied to the managed entity
            assertThat(stubReview.getRating()).isEqualTo(2);
            assertThat(stubReview.getReview()).isEqualTo("Changed my mind.");
        }

        @Test
        @DisplayName("Throws ItemReviewNotFoundException when review does not exist")
        void update_reviewNotFound_throwsException() {
            when(itemReviewRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.update(999L, new ItemReviewRequest(5, "Good")))
                    .isInstanceOf(ItemReviewNotFoundException.class);
        }
    }

    // ---------------------------------------------------------------
    // delete()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Deletes the review when it exists")
        void delete_existingReview_deletesSuccessfully() {
            when(itemReviewRepository.findById(100L)).thenReturn(Optional.of(stubReview));

            reviewService.delete(100L);

            verify(itemReviewRepository).delete(stubReview);
        }

        @Test
        @DisplayName("Throws ItemReviewNotFoundException when review does not exist")
        void delete_reviewNotFound_throwsException() {
            when(itemReviewRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.delete(999L))
                    .isInstanceOf(ItemReviewNotFoundException.class);

            verify(itemReviewRepository, never()).delete(any());
        }
    }
}