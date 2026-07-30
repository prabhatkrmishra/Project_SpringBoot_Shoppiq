package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link CategoryWriteService} implementation providing transactional persistence
 * for category entities.
 *
 * @author prabhatkrmishra
 * @see CategoryWriteService
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class CategoryWriteServiceImpl implements CategoryWriteService {

    private final CategoryRepository categoryRepository;

    /**
     * Persists the given category entity.
     *
     * @param category the category entity to save
     * @return the saved category entity
     */
    @Override
    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Deletes the given category entity.
     *
     * @param category the category entity to delete
     */
    @Override
    @Transactional
    public void delete(Category category) {
        categoryRepository.delete(category);
    }
}
