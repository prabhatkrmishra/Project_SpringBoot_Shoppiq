package com.pkmprojects.shoppiq.service.category;

import com.pkmprojects.shoppiq.entity.category.Category;
import com.pkmprojects.shoppiq.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link CategoryWriteService}
 * providing transactional persistence for category entities.
 *
 * <p>Thin write facade that delegates save and delete to {@code CategoryRepository}.
 * When called from {@link CategoryServiceImpl}, transactions propagate via Spring's
 * default REQUIRED propagation.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Each save/delete operation is individually transactional.</li>
 *   <li><strong>@RequiredArgsConstructor</strong> — Lombok-generated constructor injection for final fields.</li>
 * </ul>
 * </p>
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
