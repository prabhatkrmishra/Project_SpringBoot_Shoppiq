package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link CategoryWriteService}.
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class CategoryWriteServiceImpl implements CategoryWriteService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void delete(Category category) {
        categoryRepository.delete(category);
    }
}
