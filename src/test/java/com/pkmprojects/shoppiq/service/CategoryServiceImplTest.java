package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.request.CategoryRequest;
import com.pkmprojects.shoppiq.dto.response.CategoryResponse;
import com.pkmprojects.shoppiq.entity.Category;
import com.pkmprojects.shoppiq.exception.CategoryNotFoundException;
import com.pkmprojects.shoppiq.exception.DuplicateCategoryException;
import com.pkmprojects.shoppiq.repository.CategoryRepository;
import com.pkmprojects.shoppiq.service.impl.CategoryServiceImpl;
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
 * Unit tests for {@link CategoryServiceImpl}.
 *
 * <p>Covers slug generation, uniqueness enforcement, CRUD operations,
 * and error paths.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private static void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    private Category stubCategory;

    @BeforeEach
    void setUp() throws Exception {
        stubCategory = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .description("All electronic items")
                .build();
        setId(stubCategory, 1L);
    }

    // ---------------------------------------------------------------
    // create()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Creates a category with a generated slug")
        void create_validRequest_savesWithSlug() {
            CategoryRequest request = new CategoryRequest("Home Appliances", "For the home");

            when(categoryRepository.existsByNameIgnoreCase("Home Appliances")).thenReturn(false);
            when(categoryRepository.existsBySlug("home-appliances")).thenReturn(false);

            Category saved = Category.builder()
                    .name("Home Appliances")
                    .slug("home-appliances")
                    .description("For the home")
                    .build();
            when(categoryRepository.save(any(Category.class))).thenReturn(saved);

            CategoryResponse response = categoryService.create(request);

            assertThat(response.name()).isEqualTo("Home Appliances");
            assertThat(response.slug()).isEqualTo("home-appliances");

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(categoryRepository).save(captor.capture());
            assertThat(captor.getValue().getSlug()).isEqualTo("home-appliances");
        }

        @Test
        @DisplayName("Appends numeric suffix when slug already exists")
        void create_slugCollision_appendsSuffix() {
            CategoryRequest request = new CategoryRequest("Electronics", "Gadgets");

            when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(false);
            // First slug "electronics" is taken; "electronics-2" is free
            when(categoryRepository.existsBySlug("electronics")).thenReturn(true);
            when(categoryRepository.existsBySlug("electronics-2")).thenReturn(false);

            Category saved = Category.builder()
                    .name("Electronics")
                    .slug("electronics-2")
                    .description("Gadgets")
                    .build();
            when(categoryRepository.save(any(Category.class))).thenReturn(saved);

            CategoryResponse response = categoryService.create(request);

            assertThat(response.slug()).isEqualTo("electronics-2");
        }

        @Test
        @DisplayName("Throws DuplicateCategoryException when name already exists")
        void create_duplicateName_throwsException() {
            CategoryRequest request = new CategoryRequest("Electronics", "Gadgets");
            when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

            assertThatThrownBy(() -> categoryService.create(request))
                    .isInstanceOf(DuplicateCategoryException.class);

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws NullPointerException when request is null")
        void create_nullRequest_throwsNullPointerException() {
            assertThatThrownBy(() -> categoryService.create(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ---------------------------------------------------------------
    // update()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Updates description without changing the slug when name is unchanged")
        void update_sameNameDifferentDescription_updatesDescription() {
            CategoryRequest request = new CategoryRequest("Electronics", "Updated description");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(stubCategory));
            when(categoryRepository.save(stubCategory)).thenReturn(stubCategory);

            CategoryResponse response = categoryService.update(1L, request);

            assertThat(response.name()).isEqualTo("Electronics");
            assertThat(stubCategory.getDescription()).isEqualTo("Updated description");
        }

        @Test
        @DisplayName("Throws DuplicateCategoryException when new name conflicts with another category")
        void update_duplicateName_throwsException() {
            CategoryRequest request = new CategoryRequest("Fashion", "Clothing");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(stubCategory));
            when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Fashion", 1L))
                    .thenReturn(true);

            assertThatThrownBy(() -> categoryService.update(1L, request))
                    .isInstanceOf(DuplicateCategoryException.class);

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws CategoryNotFoundException when category does not exist")
        void update_categoryNotFound_throwsException() {
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(999L, new CategoryRequest("X", "Y")))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("Throws NullPointerException when request is null")
        void update_nullRequest_throwsNullPointerException() {
            assertThatThrownBy(() -> categoryService.update(1L, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ---------------------------------------------------------------
    // delete()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Deletes an existing category")
        void delete_existingCategory_deletesSuccessfully() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(stubCategory));

            categoryService.delete(1L);

            verify(categoryRepository).delete(stubCategory);
        }

        @Test
        @DisplayName("Throws CategoryNotFoundException when category does not exist")
        void delete_categoryNotFound_throwsException() {
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.delete(999L))
                    .isInstanceOf(CategoryNotFoundException.class);

            verify(categoryRepository, never()).delete(any());
        }
    }

    // ---------------------------------------------------------------
    // getById()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("Returns a category response when category exists")
        void getById_found_returnsResponse() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(stubCategory));

            CategoryResponse response = categoryService.getById(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Electronics");
            assertThat(response.slug()).isEqualTo("electronics");
        }

        @Test
        @DisplayName("Throws CategoryNotFoundException when category does not exist")
        void getById_notFound_throwsException() {
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getById(999L))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    // ---------------------------------------------------------------
    // getBySlug()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getBySlug()")
    class GetBySlug {

        @Test
        @DisplayName("Returns a category response when slug matches")
        void getBySlug_found_returnsResponse() {
            when(categoryRepository.findBySlug("electronics"))
                    .thenReturn(Optional.of(stubCategory));

            CategoryResponse response = categoryService.getBySlug("electronics");

            assertThat(response.slug()).isEqualTo("electronics");
            assertThat(response.name()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("Throws CategoryNotFoundException when slug does not match any category")
        void getBySlug_notFound_throwsException() {
            when(categoryRepository.findBySlug("unknown-slug")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getBySlug("unknown-slug"))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    // ---------------------------------------------------------------
    // getAll()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("Returns all categories ordered by name")
        void getAll_multipleCategories_returnsAll() throws Exception {
            Category fashion = Category.builder()
                    .name("Fashion")
                    .slug("fashion")
                    .description("Clothing")
                    .build();
            setId(fashion, 2L);

            when(categoryRepository.findAllByOrderByNameAsc())
                    .thenReturn(List.of(stubCategory, fashion));

            List<CategoryResponse> result = categoryService.getAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("Electronics");
            assertThat(result.get(1).name()).isEqualTo("Fashion");
        }

        @Test
        @DisplayName("Returns an empty list when no categories exist")
        void getAll_noCategories_returnsEmptyList() {
            when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

            List<CategoryResponse> result = categoryService.getAll();

            assertThat(result).isEmpty();
        }
    }
}