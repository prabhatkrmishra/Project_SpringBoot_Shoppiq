package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link CategoryLookupService}.
 *
 * <p>All queries run in a read-only transaction. Delegates directly
 * to {@code CategoryRepository} with appropriate sort ordering.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CategoryLookupServiceImpl implements CategoryLookupService {

    private final CategoryRepository categoryRepository;

    @Override
    public Optional<Category> findById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    @Override
    public long count() {
        return categoryRepository.count();
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Override
    public Page<Category> findAll(int page, int size) {
        return categoryRepository.findAllByOrderByNameAsc(PageRequest.of(page, size));
    }

    @Override
    public Page<Category> findByNameOrDescriptionContaining(String search, int page, int size) {
        return categoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                search, search, PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    @Override
    public List<Object[]> findTopSellingCategoryIds(Instant since, int size) {
        return categoryRepository.findTopSellingCategoryIds(since, size);
    }

    @Override
    public List<Category> findAllByIds(List<Long> ids) {
        return categoryRepository.findAllById(ids);
    }
}
